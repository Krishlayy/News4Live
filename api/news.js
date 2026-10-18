export default async function handler(req, res) {
  const { per_page = 10, page = 1, category = '' } = req.query;

  try {
    let url = `https://news4live.com/wp-json/wp/v2/posts?per_page=${per_page}&page=${page}&_embed=1`;
    if (category) {
      url += `&categories=${category}`;
    }

    const response = await fetch(url, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': 'application/json, text/plain, */*'
      }
    });

    if (!response.ok) {
      return res.status(response.status).json({ error: `WordPress API responded with status ${response.status}` });
    }

    const posts = await response.json();
    return res.status(200).json(posts);
  } catch (error) {
    return res.status(500).json({ error: error.message || 'Failed to fetch WordPress posts' });
  }
}
