#!/usr/bin/env python3
"""
Fetch LOTS of real Christian content, properly categorized and filtered.
"""
import sys, os, json, urllib.request, urllib.parse, xml.etree.ElementTree as ET, re
from email.utils import parsedate_to_datetime

def itunes_search(term, limit=5):
    url = f"https://itunes.apple.com/search?term={urllib.parse.quote(term)}&media=podcast&limit={limit}"
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req, timeout=10) as resp:
            return [(r.get('collectionName',''), r.get('feedUrl',''), r.get('artworkUrl600') or r.get('artworkUrl100',''), r.get('artistName','')) for r in json.loads(resp.read()).get('results',[]) if r.get('feedUrl')]
    except: return []

def parse_feed(url, name, art, artist):
    try:
        req = urllib.request.Request(url, headers={'User-Agent':'Mozilla/5.0'})
        with urllib.request.urlopen(req, timeout=12) as resp:
            root = ET.fromstring(resp.read())
        eps = []
        for item in root.iter('item'):
            title = (item.findtext('title') or '').strip()
            audio = None
            for enc in item.findall('enclosure'):
                u = enc.get('url',''); t = enc.get('type','') or ''
                if 'audio' in t or u.endswith('.mp3') or u.endswith('.m4a'):
                    audio = u; break
            dur_ms = 0
            for ns in ['http://www.itunes.com/dtds/podcast-1.0.dtd','http://www.itunes.com/DTDs/Podcast-1.0.dtd']:
                d = item.find(f'{{{ns}}}duration')
                if d is not None and d.text:
                    p = d.text.strip().split(':')
                    try:
                        if len(p)==3: dur_ms=(int(p[0])*3600+int(p[1])*60+int(p[2]))*1000
                        elif len(p)==2: dur_ms=(int(p[0])*60+int(p[1]))*1000
                        else: dur_ms=int(p[0])*1000
                    except: pass
                    break
            pub = 0
            pd = item.findtext('pubDate') or ''
            if pd:
                try: pub = int(parsedate_to_datetime(pd).timestamp()*1000)
                except: pass
            ep_art = art
            for ns in ['http://www.itunes.com/dtds/podcast-1.0.dtd','http://www.itunes.com/DTDs/Podcast-1.0.dtd']:
                img = item.find(f'{{{ns}}}image')
                if img is not None and img.get('href'): ep_art = img.get('href'); break
            desc = re.sub(r'<[^>]+>','', (item.findtext('description') or '')[:300]).strip() or f"Episode from {name}"
            if audio and title:
                eps.append({'title':title,'description':desc,'audioUrl':audio,'durationMs':dur_ms,'thumbnailUrl':ep_art,'publishedAt':pub,'podcastName':name,'speaker':artist})
        return eps
    except: return []

NON_CHRISTIAN = ['vanderpump', 'paranormal', 'heist', 'heuermann', 'murder', 'true crime', 'bot', 'lgbtq', 'supernatural', 'conspiracy', 'ghost', 'haunted', 'alien', 'ufo', 'crypto', 'politics', 'political', 'election', 'partisan', 'secular', 'atheist', 'skeptic', 'comedy central', 'gossip', 'celebrity', 'pop culture']
CHRISTIAN_VERIFY = ['jesus', 'christ', 'christian', 'gospel', 'bible', 'scripture', 'church', 'faith', 'god', 'lord', 'worship', 'prayer', 'sermon', 'preach', 'pastor', 'theology', 'ministry', 'devotional', 'salvation', 'grace', 'holy', 'spirit', 'spiritual', 'telugu christian', 'hindi christian', 'tamil christian', 'malayalam christian', 'bible study', 'apologetics', 'testimony', 'discipleship', 'covenant', 'prophetic', 'kingdom', 'righteous', 'hymn', 'praise', 'evangelism', 'missionary', 'sanctification', 'creation', 'cross', 'resurrection', 'repentance', 'blessed', 'mercy', 'glory', 'almighty', 'savior', 'redeemer']

def is_christian(ep):
    text = (ep.get('title','') + ' ' + ep.get('description','') + ' ' + ep.get('speaker','') + ' ' + ep.get('podcastName','')).lower()
    for bad in NON_CHRISTIAN:
        if bad in text: return False
    for good in CHRISTIAN_VERIFY:
        if good in text: return True
    return False

WORSHIP_SEARCH = [
    "Hillsong Worship", "Hillsong United", "Bethel Music", "Elevation Worship",
    "Maverick City Music", "Jesus Culture", "Planetshakers", "Chris Tomlin",
    "Matt Redman", "Lauren Daigle", "Don Moen", "Michael W Smith",
    "Casting Crowns", "Mercy Me", "Third Day", "Kari Jobe",
    "Kirk Franklin", "Tasha Cobbs", "Travis Greene", "William McDowell",
    "Integrity Music", "Hosanna Music",
    "Telugu Christian worship", "Telugu Jesus songs",
    "Hindi Christian worship", "Hindi Gospel songs",
    "Tamil Christian worship", "Tamil Jesus songs",
    "Malayalam Christian songs", "Malayalam worship",
    "Kannada Christian songs", "Don Moen worship",
    "Contemporary Christian music", "Christian hymns",
]
SERMON_SEARCH = [
    "John Piper sermon", "Timothy Keller sermon", "John MacArthur sermon",
    "Paul Washer sermon", "Voddie Baucham sermon", "Charles Stanley sermon",
    "Alistair Begg sermon", "R.C. Sproul sermon", "Tony Evans sermon",
    "David Jeremiah sermon", "Francis Chan sermon", "Louie Giglio sermon",
    "Matt Chandler sermon", "Steven Furtick sermon",
    "Telugu Christian sermon", "Tamil Christian sermon",
    "Malayalam Christian sermon", "Hindi Christian sermon",
    "Bible study daily", "Expository preaching",
]
DEBATE_SEARCH = [
    "Christian apologetics", "Ravi Zacharias", "William Lane Craig",
    "Frank Turek", "John Lennox", "Stand to Reason",
    "Cross Examined", "Cold Case Christianity",
    "Christian debate theology", "Apologetics debate",
]
TESTIMONY_SEARCH = [
    "Christian testimony podcast", "Conversion story Christian",
    "Christian testimony", "I found Jesus testimony",
]
QA_SEARCH = [
    "Christian Q&A podcast", "Bible questions answered",
    "Ask the pastor", "Open line Christian radio",
]

def main():
    os.makedirs('/home/z/my-project/scripts', exist_ok=True)
    all_eps = []
    categories = [
        ("WORSHIP", WORSHIP_SEARCH, "Worship Songs"),
        ("SERMON", SERMON_SEARCH, "Sermons"),
        ("DEBATE", DEBATE_SEARCH, "Debates"),
        ("TESTIMONY", TESTIMONY_SEARCH, "Testimonies"),
        ("QA", QA_SEARCH, "Q&A Programs"),
    ]
    for cat_code, search_list, cat_name in categories:
        print(f"\nFetching {cat_name}...")
        seen = set(); feeds = []
        for term in search_list:
            for name, url, art, artist in itunes_search(term, 3):
                if url not in seen: seen.add(url); feeds.append((name, url, art, artist))
            sys.stdout.write('.'); sys.stdout.flush()
        print(f"  {len(feeds)} feeds")
        for name, url, art, artist in feeds:
            combined = (name + artist).lower()
            if 'telugu' in combined: lang = 'TE'
            elif 'hindi' in combined: lang = 'HI'
            elif 'tamil' in combined: lang = 'TA'
            elif 'malayalam' in combined: lang = 'ML'
            elif 'kannada' in combined: lang = 'KN'
            else: lang = 'EN'
            eps = parse_feed(url, name, art, artist)
            for ep in eps[:4]:
                if is_christian(ep):
                    ep['id'] = f"ep_{len(all_eps):04d}"
                    ep['type'] = 'PODCAST' if cat_code == 'QA' else cat_code
                    ep['category'] = 'TEACHING' if cat_code in ('QA','SERMON') else cat_code
                    ep['language'] = lang
                    ep['isLive'] = False
                    ep['isDownloadable'] = True
                    ep['listenerCount'] = 0
                    all_eps.append(ep)
            if len(all_eps) >= 200: break
        if len(all_eps) >= 200: break

    seen_urls = set(); deduped = []
    for ep in all_eps:
        if ep['audioUrl'] not in seen_urls:
            seen_urls.add(ep['audioUrl']); deduped.append(ep)

    print(f"\nTOTAL: {len(deduped)} filtered Christian episodes")
    cats = {}
    langs = {}
    for e in deduped:
        cats[e['category']] = cats.get(e['category'], 0) + 1
        langs[e['language']] = langs.get(e['language'], 0) + 1
    print(f"Categories: {cats}")
    print(f"Languages: {langs}")

    with open('/home/z/my-project/scripts/real-episodes.json','w') as f:
        json.dump(deduped, f, indent=2, default=str)
    print(f"Saved {len(deduped)} episodes")

if __name__=='__main__':
    main()
