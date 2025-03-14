import ffmpeg

def compress_mp3(input_file, output_file, bitrate="48k"):
    try:
        (
            ffmpeg
            .input(input_file)
            .output(output_file, **{"map": "0:a:0", "b:a": bitrate})
            .run(quiet=True, capture_stdout=True, capture_stderr=True)
        )
        print(f"Conversion successful: {output_file}")
    except Exception as e:
        print(f"Error during conversion: {e}")