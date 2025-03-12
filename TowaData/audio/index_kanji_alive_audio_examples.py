import re
import os

KANJI_ALIVE_AUDIO_DIR = "kanji_alive/"
KANJI_ALIVE_INDEX_PATH = "kanji_alive_index.csv"

if __name__ == "__main__":
    with open(KANJI_ALIVE_INDEX_PATH, "r", encoding="utf-8-sig") as f:
        for i, line in enumerate(f):
            if (i == 0): continue

            split = line.split(",")
            examples = split[9]
            audio_name_prefix_start = 2
            audio_name_prefix_end = line.find(",", 2)
            audio_name_prefix = line[audio_name_prefix_start : audio_name_prefix_end]

            examples_start = line.find("[")
            examples_end = line.rfind("]")
            examples_str = line[examples_start : examples_end]
            
            examples_split = examples_str.split("\"\"")
            examples_cleaned = examples_split[1::4]
            
            reg = r'\（[\s\S]*\）'
            examples = [re.sub(reg, '', e) for e in examples_cleaned]

            # Post process any weirdness
            for i, e in enumerate(examples):
                examples[i] = e.replace("*", "")
                if "じる/ずる" in e:
                    examples.append(e.replace("じる/ずる", "ずる"))
                    examples[i] = e.replace("じる/ずる", "じる")

            for i in range(0, len(examples)):
                filename = os.path.join(KANJI_ALIVE_AUDIO_DIR, f"{audio_name_prefix}_06_{chr(ord('a') + i)}.mp3")

                if os.path.exists(filename):
                    new_name = os.path.join(KANJI_ALIVE_AUDIO_DIR, f"{examples[i]}.mp3")

                    ext = 2
                    while os.path.exists(new_name):
                        new_name = os.path.join(KANJI_ALIVE_AUDIO_DIR, f"{examples[i]}_{ext}.mp3")
                        ext += 1
                    
                    os.rename(filename, new_name)
                    print(f"Renamed \"{filename}\" -> \"{new_name}\"")

    print("done.")


