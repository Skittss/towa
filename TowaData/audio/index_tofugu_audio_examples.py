import re
import os

import compress_audio

TOFUGU_AUDIO_DIR = "tofugu/"
AUDIO_OUTPUT_DIR = "../../app/src/main/assets/audio/tofugu/"

if __name__ == "__main__":
  if not os.path.exists(AUDIO_OUTPUT_DIR):
    os.makedirs(AUDIO_OUTPUT_DIR)
	
    mp3s = [f for f in os.listdir(TOFUGU_AUDIO_DIR) if f.endswith(".mp3")]
    reg = r'\【[\s\S]*\】'
    
    for i, f in enumerate(mp3s):
        if "【" in f and "】" in f:
            filename = os.path.join(TOFUGU_AUDIO_DIR, f)
            new_name = os.path.join(AUDIO_OUTPUT_DIR, re.sub(reg, '', f))
            compress_audio.compress_mp3(filename, new_name)
            print(f"[{i}/{len(mp3s)}: \"{filename}\" -> \"{new_name}\"")

    print("done.")