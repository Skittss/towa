import re
import os

TOFUGU_AUDIO_DIR = "tofugu/"

if __name__ == "__main__":
    mp3s = [f for f in os.listdir(TOFUGU_AUDIO_DIR) if f.endswith(".mp3")]
    reg = r'\【[\s\S]*\】'
    
    for f in mp3s:
        if "【" in f and "】" in f:
            filename = os.path.join(TOFUGU_AUDIO_DIR, f)
            new_name = re.sub(reg, '', filename)
            os.rename(filename, new_name)
            print(f"Renamed \"{filename}\" -> \"{new_name}\"")

    print("done.")