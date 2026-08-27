import os
import re
import json
import time
import urllib.request
import cloudinary
import cloudinary.uploader

# Paths
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ENV_FILE = os.path.join(BASE_DIR, '.env')
SEEDER_FILE = os.path.join(BASE_DIR, 'src', 'main', 'java', 'com', 'keshav', 'config', 'DataSeeder.java')
MAPPING_FILE = os.path.join(BASE_DIR, 'scripts', 'unsplash_cloudinary_mapping.json')

# Load .env
env = {}
if os.path.exists(ENV_FILE):
    with open(ENV_FILE, 'r', encoding='utf-8') as f:
        for line in f:
            if '=' in line:
                k, v = line.strip().split('=', 1)
                env[k.strip()] = v.strip().rstrip(';')

cloudinary.config(
    cloud_name=env.get('CLOUDINARY_CLOUD_NAME', 'oqmadwpj'),
    api_key=env.get('CLOUDINARY_API_KEY', '311644574419542'),
    api_secret=env.get('CLOUDINARY_API_SECRET', 'qXChiXs65JQHyNA-s6Utx0DEFz8')
)

print(f"Configured Cloudinary for cloud: {env.get('CLOUDINARY_CLOUD_NAME')}")

# Load existing mapping if available
url_mapping = {}
if os.path.exists(MAPPING_FILE):
    try:
        with open(MAPPING_FILE, 'r', encoding='utf-8') as f:
            url_mapping = json.load(f)
    except Exception:
        url_mapping = {}

# Read DataSeeder.java to find all Unsplash URLs
with open(SEEDER_FILE, 'r', encoding='utf-8') as f:
    seeder_content = f.read()

unsplash_urls = list(set(re.findall(r'https://images\.unsplash\.com/[^\s",]+', seeder_content)))
print(f"Found {len(unsplash_urls)} unique Unsplash URLs to migrate to Cloudinary.")

success_count = 0
fail_count = 0

for idx, url in enumerate(unsplash_urls, 1):
    if url in url_mapping and url_mapping[url].startswith('https://res.cloudinary.com'):
        print(f"[{idx}/{len(unsplash_urls)}] Already migrated: {url[:50]}... -> {url_mapping[url]}")
        continue

    print(f"[{idx}/{len(unsplash_urls)}] Uploading to Cloudinary: {url[:60]}...")
    try:
        res = cloudinary.uploader.upload(
            url,
            folder="ecommerce/products",
            resource_type="image"
        )
        secure_url = res.get('secure_url')
        if secure_url:
            url_mapping[url] = secure_url
            success_count += 1
            print(f"  -> SUCCESS: {secure_url}")
        else:
            print(f"  -> FAILED: No secure_url in response: {res}")
            fail_count += 1
    except Exception as e:
        print(f"  -> ERROR uploading {url}: {e}")
        fail_count += 1
    
    # Save progress mapping
    with open(MAPPING_FILE, 'w', encoding='utf-8') as f:
        json.dump(url_mapping, f, indent=2)
    
    time.sleep(0.2)

print(f"\nUpload complete! Migrated: {success_count}, Failed: {fail_count}, Total in Mapping: {len(url_mapping)}")

# Now replace all Unsplash URLs in DataSeeder.java with their Cloudinary counterparts
updated_seeder_content = seeder_content
replaced_count = 0

for unsplash_url, cloudinary_url in url_mapping.items():
    if unsplash_url in updated_seeder_content:
        updated_seeder_content = updated_seeder_content.replace(unsplash_url, cloudinary_url)
        replaced_count += 1

with open(SEEDER_FILE, 'w', encoding='utf-8') as f:
    f.write(updated_seeder_content)

print(f"Updated DataSeeder.java with {replaced_count} Cloudinary image URLs.")
