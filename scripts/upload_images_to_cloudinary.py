import os
import re
import json
import cloudinary
import cloudinary.uploader

# Paths
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ENV_FILE = os.path.join(BASE_DIR, '.env')
PUBLIC_IMAGES_DIR = os.path.join(BASE_DIR, '..', 'Frontend', 'public', 'images')
FRONTEND_SRC_DIR = os.path.join(BASE_DIR, '..', 'Frontend', 'src')

# Load .env
env = {}
if os.path.exists(ENV_FILE):
    with open(ENV_FILE, 'r', encoding='utf-8') as f:
        for line in f:
            if '=' in line:
                k, v = line.strip().split('=', 1)
                env[k.strip()] = v.strip().rstrip(';')

cloudinary.config(
    cloud_name=env.get('CLOUDINARY_CLOUD_NAME'),
    api_key=env.get('CLOUDINARY_API_KEY'),
    api_secret=env.get('CLOUDINARY_API_SECRET')
)

print(f"Cloudinary configured for cloud: {env.get('CLOUDINARY_CLOUD_NAME')}")

# Load existing mapping if available
mapping_file = os.path.join(BASE_DIR, 'scripts', 'image_url_mapping.json')
url_mapping = {}
if os.path.exists(mapping_file):
    with open(mapping_file, 'r', encoding='utf-8') as f:
        url_mapping = json.load(f)

# Upload local files from Frontend/public/images/
if os.path.exists(PUBLIC_IMAGES_DIR):
    local_files = [f for f in os.listdir(PUBLIC_IMAGES_DIR) if f.endswith(('.png', '.jpg', '.jpeg', '.webp'))]
    print(f"Found {len(local_files)} local image files in Frontend/public/images/")
    
    for filename in local_files:
        file_path = os.path.join(PUBLIC_IMAGES_DIR, filename)
        local_ref = f"/images/{filename}"
        
        try:
            print(f"Uploading local file: {filename}...")
            res = cloudinary.uploader.upload(file_path, folder="ecommerce/products")
            secure_url = res.get('secure_url')
            if secure_url:
                url_mapping[local_ref] = secure_url
                print(f" -> Uploaded {local_ref} to {secure_url}")
        except Exception as e:
            print(f" -> Error uploading {filename}: {e}")

# Save updated mapping
with open(mapping_file, 'w', encoding='utf-8') as f:
    json.dump(url_mapping, f, indent=2)

print(f"Saved updated mapping file to {mapping_file}")

# Recursively scan all files in Frontend/src to replace image references
files_updated = 0
total_replacements = 0

for root, dirs, files in os.walk(FRONTEND_SRC_DIR):
    for file in files:
        if file.endswith(('.ts', '.tsx', '.js', '.jsx', '.json')):
            file_path = os.path.join(root, file)
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            modified = False
            for orig_ref, cloud_url in url_mapping.items():
                if orig_ref in content:
                    content = content.replace(orig_ref, cloud_url)
                    modified = True
                    total_replacements += 1
            
            if modified:
                with open(file_path, 'w', encoding='utf-8') as f:
                    f.write(content)
                files_updated += 1
                print(f"Updated {os.path.relpath(file_path, FRONTEND_SRC_DIR)}")

print(f"Done! Replaced {total_replacements} total image references across {files_updated} files in Frontend/src.")
