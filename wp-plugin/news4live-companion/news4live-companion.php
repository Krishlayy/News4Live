<?php
/**
 * Plugin Name: News 4 Live App Companion
 * Plugin URI: https://news4live.com/
 * Description: Official high-performance REST API companion and Firebase Cloud Messaging (FCM) push notification bridge for the News 4 Live Android application.
 * Version: 1.1.0
 * Author: News 4 Live Team
 * Author URI: https://news4live.com/
 * License: GPL-2.0+
 * Text Domain: news4live-companion
 */

if (!defined('ABSPATH')) {
    exit; // Exit if accessed directly
}

class News4Live_Companion_Plugin {

    const VERSION = '1.1.0';
    const API_NAMESPACE = 'news4live/v1';
    const HOME_TRANSIENT_KEY = 'news4live_home_feed_cache';
    const TRANSIENT_EXPIRY = 120; // 2 minutes cache

    public function __construct() {
        // Register custom REST API routes
        add_action('rest_api_init', array($this, 'register_rest_routes'));

        // Push notification on post publish
        add_action('transition_post_status', array($this, 'handle_post_publish'), 10, 3);

        // Cache Invalidation on post updates
        add_action('save_post', array($this, 'invalidate_cache'));
        add_action('deleted_post', array($this, 'invalidate_cache'));

        // Google AdSense Website Ads Injection
        add_action('wp_head', array($this, 'inject_adsense_head_code'), 5);
        add_filter('the_content', array($this, 'inject_in_article_ads'));

        // Admin settings
        if (is_admin()) {
            add_action('admin_menu', array($this, 'add_admin_menu'));
            add_action('admin_init', array($this, 'register_settings'));
            add_action('add_meta_boxes', array($this, 'add_breaking_meta_box'));
            add_action('save_post', array($this, 'save_breaking_meta_box'));
        }
    }

    /**
     * Invalidate transient cache when any post changes
     */
    public function invalidate_cache() {
        delete_transient(self::HOME_TRANSIENT_KEY);
    }

    /**
     * Register high-performance custom REST API routes
     */
    public function register_rest_routes() {
        // Aggregated Home endpoint with transient caching
        register_rest_route(self::API_NAMESPACE, '/home', array(
            'methods'  => 'GET',
            'callback' => array($this, 'get_home_feed'),
            'permission_callback' => '__return_true',
        ));

        // Breaking News endpoint
        register_rest_route(self::API_NAMESPACE, '/breaking', array(
            'methods'  => 'GET',
            'callback' => array($this, 'get_breaking_news'),
            'permission_callback' => '__return_true',
        ));

        // Trending News endpoint
        register_rest_route(self::API_NAMESPACE, '/trending', array(
            'methods'  => 'GET',
            'callback' => array($this, 'get_trending_news'),
            'permission_callback' => '__return_true',
        ));
    }

    /**
     * Aggregated Home Feed Handler with Transient Cache
     */
    public function get_home_feed($request) {
        // Check transient cache first
        $cached_feed = get_transient(self::HOME_TRANSIENT_KEY);
        if ($cached_feed !== false && !empty($cached_feed)) {
            return rest_ensure_response($cached_feed);
        }

        $breaking_cat_id = 31; // 'ब्रेकिंग न्यूज़'
        $top_cat_id      = 18; // 'टॉप न्यूज़'

        // 1. Breaking News
        $breaking_posts = $this->query_posts_formatted(array(
            'cat'            => $breaking_cat_id,
            'posts_per_page' => 5,
        ));

        // 2. Featured / Top Stories
        $featured_posts = $this->query_posts_formatted(array(
            'cat'            => $top_cat_id,
            'posts_per_page' => 6,
        ));

        // 3. Latest News
        $latest_posts = $this->query_posts_formatted(array(
            'posts_per_page' => 10,
        ));

        // 4. Categories list with counts
        $raw_cats = get_categories(array(
            'hide_empty' => true,
            'exclude'    => array(1), // Exclude Uncategorized
            'orderby'    => 'count',
            'order'      => 'DESC',
        ));

        $categories = array();
        foreach ($raw_cats as $cat) {
            $categories[] = array(
                'id'          => $cat->term_id,
                'name'        => html_entity_decode($cat->name, ENT_QUOTES, 'UTF-8'),
                'slug'        => $cat->slug,
                'count'       => (int) $cat->count,
                'parent'      => (int) $cat->parent,
                'description' => $cat->description,
            );
        }

        $response_data = array(
            'breaking'   => $breaking_posts,
            'featured'   => $featured_posts,
            'latest'     => $latest_posts,
            'categories' => $categories,
            'trending'   => $featured_posts,
        );

        // Store in transient for 2 minutes
        set_transient(self::HOME_TRANSIENT_KEY, $response_data, self::TRANSIENT_EXPIRY);

        return rest_ensure_response($response_data);
    }

    public function get_breaking_news($request) {
        $limit = $request->get_param('limit') ? (int) $request->get_param('limit') : 5;
        $posts = $this->query_posts_formatted(array(
            'cat'            => 31,
            'posts_per_page' => $limit,
        ));
        return rest_ensure_response($posts);
    }

    public function get_trending_news($request) {
        $limit = $request->get_param('limit') ? (int) $request->get_param('limit') : 10;
        $posts = $this->query_posts_formatted(array(
            'cat'            => 18,
            'posts_per_page' => $limit,
        ));
        return rest_ensure_response($posts);
    }

    /**
     * Helper to query and format posts matching the Android App DTO schema
     */
    private function query_posts_formatted($args) {
        $default_args = array(
            'post_type'      => 'post',
            'post_status'    => 'publish',
            'orderby'        => 'date',
            'order'          => 'DESC',
            'no_found_rows'  => true,
            'update_post_term_cache' => true,
            'update_post_meta_cache' => true,
        );

        $query = new WP_Query(wp_parse_args($args, $default_args));
        $posts = array();

        if ($query->have_posts()) {
            while ($query->have_posts()) {
                $query->the_post();
                $post_id = get_the_ID();

                $featured_image_id = get_post_thumbnail_id($post_id);
                $full_img_url   = $featured_image_id ? wp_get_attachment_image_url($featured_image_id, 'full') : null;
                $thumb_img_url  = $featured_image_id ? wp_get_attachment_image_url($featured_image_id, 'thumbnail') : null;
                $medium_img_url = $featured_image_id ? wp_get_attachment_image_url($featured_image_id, 'medium_large') : null;

                $categories = wp_get_post_categories($post_id);

                $media_sizes = array();
                if ($featured_image_id) {
                    $sizes_meta = wp_get_attachment_metadata($featured_image_id);
                    if (!empty($sizes_meta['sizes'])) {
                        foreach ($sizes_meta['sizes'] as $s_name => $s_val) {
                            $media_sizes[$s_name] = array(
                                'file'       => $s_val['file'],
                                'width'      => $s_val['width'],
                                'height'     => $s_val['height'],
                                'source_url' => wp_get_attachment_image_url($featured_image_id, $s_name),
                            );
                        }
                    }
                }

                $posts[] = array(
                    'id'       => $post_id,
                    'date'     => get_the_date('Y-m-d\TH:i:s'),
                    'modified' => get_the_modified_date('Y-m-d\TH:i:s'),
                    'slug'     => get_post_field('post_name', $post_id),
                    'link'     => get_permalink($post_id),
                    'title'    => array('rendered' => get_the_title()),
                    'content'  => array('rendered' => apply_filters('the_content', get_the_content())),
                    'excerpt'  => array('rendered' => get_the_excerpt()),
                    'featured_media' => (int) $featured_image_id,
                    'categories'     => $categories,
                    'tags'           => array(),
                    '_embedded'      => array(
                        'author' => array(
                            array(
                                'id'   => get_the_author_meta('ID'),
                                'name' => get_the_author(),
                            )
                        ),
                        'wp:featuredmedia' => $featured_image_id ? array(
                            array(
                                'id'         => (int) $featured_image_id,
                                'source_url' => $full_img_url,
                                'title'      => array('rendered' => get_the_title()),
                                'media_details' => array(
                                    'sizes' => $media_sizes,
                                )
                            )
                        ) : array(),
                    )
                );
            }
            wp_reset_postdata();
        }

        return $posts;
    }

    /**
     * Firebase Cloud Messaging Push Notification Handler with Duplicate Prevention
     */
    public function handle_post_publish($new_status, $old_status, $post) {
        // Only fire when post transitions to 'publish'
        if ($new_status !== 'publish' || $post->post_type !== 'post') {
            return;
        }

        // Prevent duplicate notification: Check if notification was already dispatched for this post ID
        $already_sent = get_post_meta($post->ID, '_news4live_notification_sent', true);
        $resend_requested = get_post_meta($post->ID, '_news4live_resend_notification', true);

        if ($already_sent === '1' && $resend_requested !== 'yes') {
            return; // Duplicate prevented!
        }

        $fcm_server_key = get_option('news4live_fcm_server_key', '');
        if (empty($fcm_server_key)) {
            return;
        }

        $post_id   = $post->ID;
        $title     = html_entity_decode(get_the_title($post_id), ENT_QUOTES, 'UTF-8');
        $excerpt   = wp_strip_all_tags(get_the_excerpt($post_id));
        $permalink = get_permalink($post_id);
        $image_id  = get_post_thumbnail_id($post_id);
        $image_url = $image_id ? wp_get_attachment_image_url($image_id, 'medium_large') : '';

        $is_breaking = has_category(31, $post_id) || get_post_meta($post_id, '_news4live_is_breaking', true) === 'yes';
        $topic = $is_breaking ? 'breaking_news' : 'all_news';

        $payload = array(
            'to' => '/topics/' . $topic,
            'priority' => 'high',
            'data' => array(
                'post_id'     => (string) $post_id,
                'title'       => $title,
                'excerpt'     => mb_substr($excerpt, 0, 150, 'UTF-8'),
                'image'       => $image_url,
                'url'         => $permalink,
                'is_breaking' => $is_breaking ? '1' : '0',
            ),
            'notification' => array(
                'title'    => ($is_breaking ? '🔴 ब्रेकिंग: ' : '') . $title,
                'body'     => mb_substr($excerpt, 0, 100, 'UTF-8'),
                'image'    => $image_url,
                'sound'    => 'default',
            )
        );

        $response = wp_remote_post('https://fcm.googleapis.com/fcm/send', array(
            'headers' => array(
                'Authorization' => 'key=' . trim($fcm_server_key),
                'Content-Type'  => 'application/json; charset=utf-8',
            ),
            'body'        => wp_json_encode($payload),
            'timeout'     => 15,
            'blocking'    => true, // Block briefly to verify HTTP success
            'data_format' => 'body',
        ));

        // Only mark as sent if FCM returned success (HTTP 200)
        if (!is_wp_error($response) && wp_remote_retrieve_response_code($response) === 200) {
            update_post_meta($post_id, '_news4live_notification_sent', '1');
            delete_post_meta($post_id, '_news4live_resend_notification');
        }
    }

    /**
     * Admin Settings Page
     */
    public function add_admin_menu() {
        add_options_page(
            'News 4 Live App Settings',
            'News 4 Live App',
            'manage_options',
            'news4live-app',
            array($this, 'render_admin_page')
        );
    }

    /**
     * Inject Google AdSense Auto Ads / Script into website <head>
     */
    public function inject_adsense_head_code() {
        $pub_id = trim(get_option('news4live_adsense_pub_id', ''));
        $enable_auto_ads = get_option('news4live_adsense_auto_ads', 'yes');

        if (!empty($pub_id)) {
            // Normalize pub ID format (e.g. ca-pub-1234567890123456)
            if (strpos($pub_id, 'ca-pub-') === false && strpos($pub_id, 'pub-') === false) {
                $pub_id = 'ca-pub-' . $pub_id;
            } elseif (strpos($pub_id, 'pub-') === 0) {
                $pub_id = 'ca-' . $pub_id;
            }
            ?>
            <!-- Google AdSense Automated Tag by News4Live Companion -->
            <script async src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=<?php echo esc_attr($pub_id); ?>" crossorigin="anonymous"></script>
            <?php
        }
    }

    /**
     * Automatically insert AdSense responsive ad unit inside post content (after paragraph 2)
     */
    public function inject_in_article_ads($content) {
        if (!is_single() || !is_main_query()) {
            return $content;
        }

        $in_article_code = trim(get_option('news4live_adsense_in_article_code', ''));
        if (empty($in_article_code)) {
            return $content;
        }

        $paragraphs = explode('</p>', $content);
        if (count($paragraphs) > 2) {
            $ad_block = '<div class="news4live-in-article-ad" style="margin:20px 0;text-align:center;clear:both;">' . $in_article_code . '</div>';
            // Insert after 2nd paragraph
            $paragraphs[1] .= '</p>' . $ad_block;
            return implode('</p>', $paragraphs);
        }

        return $content;
    }

    public function register_settings() {
        register_setting('news4live_options_group', 'news4live_fcm_server_key');
        register_setting('news4live_options_group', 'news4live_adsense_pub_id');
        register_setting('news4live_options_group', 'news4live_adsense_auto_ads');
        register_setting('news4live_options_group', 'news4live_adsense_in_article_code');
    }

    public function render_admin_page() {
        ?>
        <div class="wrap">
            <h1>News 4 Live — Settings & Monetization</h1>
            <p>Configure Firebase Cloud Messaging (FCM) for app push notifications and Google AdSense for website monetization.</p>
            <form method="post" action="options.php">
                <?php
                settings_fields('news4live_options_group');
                do_settings_sections('news4live_options_group');
                ?>
                <hr />
                <h2>💰 Google AdSense Integration (Website)</h2>
                <p>Monetize your website (<code>news4live.com</code>) with official Google AdSense auto ads and in-article banner units.</p>
                <table class="form-table">
                    <tr valign="top">
                        <th scope="row">AdSense Publisher ID (Client ID)</th>
                        <td>
                            <input type="text" name="news4live_adsense_pub_id" value="<?php echo esc_attr(get_option('news4live_adsense_pub_id')); ?>" class="regular-text" placeholder="ca-pub-1234567890123456" />
                            <p class="description">Enter your Google AdSense Publisher ID (found in AdSense -> Account -> Settings -> Account information).</p>
                        </td>
                    </tr>
                    <tr valign="top">
                        <th scope="row">In-Article Ad Unit Code (Optional)</th>
                        <td>
                            <textarea name="news4live_adsense_in_article_code" rows="5" cols="50" class="large-text code" placeholder="<ins class='adsbygoogle' ...></ins><script>(adsbygoogle = window.adsbygoogle || []).push({});</script>"><?php echo esc_textarea(get_option('news4live_adsense_in_article_code')); ?></textarea>
                            <p class="description">Optional custom AdSense ad unit code. If provided, it will be placed inside single news articles after paragraph 2.</p>
                        </td>
                    </tr>
                </table>

                <hr />
                <h2>🔔 Firebase Push Notifications (Android App)</h2>
                <table class="form-table">
                    <tr valign="top">
                        <th scope="row">Firebase Cloud Messaging (FCM) Server Key</th>
                        <td>
                            <input type="password" name="news4live_fcm_server_key" value="<?php echo esc_attr(get_option('news4live_fcm_server_key')); ?>" class="regular-text" />
                            <p class="description">Enter your Firebase Cloud Messaging Server Key from Firebase Console -> Project Settings -> Cloud Messaging.</p>
                        </td>
                    </tr>
                </table>
                <?php submit_button('Save Settings'); ?>
            </form>
            <hr />
            <h2>API Diagnostics & Status</h2>
            <p><strong>Custom Aggregated Endpoint:</strong> <code><?php echo esc_url(rest_url('news4live/v1/home')); ?></code></p>
            <p><strong>Transient Cache Status:</strong> <?php echo get_transient(self::HOME_TRANSIENT_KEY) ? '<span style="color:green;font-weight:bold;">Cached (Active)</span>' : '<span style="color:orange;">Not Cached (Will regenerate on next call)</span>'; ?></p>
        </div>
        <?php
    }

    /**
     * Breaking News Meta Box in Post Editor
     */
    public function add_breaking_meta_box() {
        add_meta_box(
            'news4live_breaking_box',
            'News 4 Live App — Notification Controls',
            array($this, 'render_breaking_meta_box'),
            'post',
            'side',
            'high'
        );
    }

    public function render_breaking_meta_box($post) {
        $is_breaking = get_post_meta($post->ID, '_news4live_is_breaking', true);
        $already_sent = get_post_meta($post->ID, '_news4live_notification_sent', true);
        ?>
        <p>
            <label>
                <input type="checkbox" name="news4live_is_breaking" value="yes" <?php checked($is_breaking, 'yes'); ?> />
                <strong>Mark as Breaking News</strong> (🔴 Alert)
            </label>
        </p>
        <?php if ($already_sent === '1') : ?>
            <p style="color:#666; font-size:12px;">
                <em>✓ Push notification was already sent for this post.</em>
            </p>
            <p>
                <label>
                    <input type="checkbox" name="news4live_resend_notification" value="yes" />
                    Resend Push Notification on Update
                </label>
            </p>
        <?php else : ?>
            <p style="color:#666; font-size:12px;">
                <em>Push notification will be sent automatically when published.</em>
            </p>
        <?php endif; ?>
        <?php
    }

    public function save_breaking_meta_box($post_id) {
        if (defined('DOING_AUTOSAVE') && DOING_AUTOSAVE) return;
        if (!current_user_can('edit_post', $post_id)) return;

        if (isset($_POST['news4live_is_breaking'])) {
            update_post_meta($post_id, '_news4live_is_breaking', 'yes');
        } else {
            delete_post_meta($post_id, '_news4live_is_breaking');
        }

        if (isset($_POST['news4live_resend_notification'])) {
            update_post_meta($post_id, '_news4live_resend_notification', 'yes');
        }
    }
}

new News4Live_Companion_Plugin();
