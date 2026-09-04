package com.example.data.model

enum class AnimationStyle(val displayName: String, val description: String) {
    TWO_D_CARTOON("2D Cartoon", "Classic hand-drawn & flat graphic vector style"),
    THREE_D_ANIMATION("3D Animation", "Pixar & DreamWorks cinematic 3D CGI"),
    ANIME("Anime", "Japanese animation with expressive eyes and dynamic shading"),
    CINEMATIC("Cinematic", "Photorealistic rendering with dramatic lighting & lens flares"),
    REALISTIC("Realistic", "Hyper-detailed textures and lifelike rendering"),
    AFRICAN_ANIMATION("African Animation", "Vibrant Afrofuturist aesthetics, rich Ankara patterns & storytelling"),
    CHILDRENS_ANIMATION("Children's Animation", "Soft rounded shapes, warm joyful colors & playful textures"),
    COMIC("Comic", "Graphic novel halftone dots, bold ink lines & dramatic angles"),
    FANTASY("Fantasy", "Magical particle glows, mythical environments & ethereal light")
}

enum class AspectRatioOption(val label: String, val ratioValue: Float, val widthDp: Int, val heightDp: Int) {
    RATIO_16_9("16:9 Landscape (YouTube, TV)", 16f / 9f, 320, 180),
    RATIO_9_16("9:16 Portrait (TikTok, Reels)", 9f / 16f, 180, 320),
    RATIO_1_1("1:1 Square (Instagram, Feed)", 1f, 220, 220),
    RATIO_4_5("4:5 Social (Portrait Post)", 4f / 5f, 200, 250)
}

enum class CameraMovement(val label: String) {
    STATIC("Static (Fixed Camera)"),
    PAN_LEFT("Pan Left"),
    PAN_RIGHT("Pan Right"),
    ZOOM_IN("Smooth Zoom In"),
    ZOOM_OUT("Dramatic Zoom Out"),
    DRONE_ORBIT("360° Drone Orbit"),
    CRANE_UP("Epic Crane Up")
}

enum class CharacterMovement(val label: String) {
    WALK("Walk Forward"),
    RUN("Run Fast"),
    SIT("Sit Down"),
    STAND("Stand Confident"),
    WAVE("Wave Hand"),
    JUMP("Jump in Joy"),
    DANCE("Dance Afrobeats"),
    TALK("Talk & Explain"),
    LAUGH("Laugh Heartily"),
    CRY("Emotional Crying"),
    POINT("Point to Distance"),
    TURN("Turn Around"),
    FIGHT("Martial Action / Fight"),
    LOOK_AROUND("Look Around Suspiciously")
}

enum class VoiceLanguage(val displayName: String) {
    ENGLISH("English (Global)"),
    NIGERIAN_ENGLISH("Nigerian English"),
    PIDGIN_ENGLISH("Pidgin English (Naija)"),
    IGBO("Igbo"),
    YORUBA("Yoruba"),
    HAUSA("Hausa")
}

data class VoiceProfile(
    val id: String,
    val name: String,
    val gender: String,
    val ageStyle: String,
    val language: VoiceLanguage,
    val accent: String,
    val sampleText: String
)

object VoicePresets {
    val allVoices = listOf(
        VoiceProfile("v_amaka", "Amaka", "Female", "Young Adult", VoiceLanguage.NIGERIAN_ENGLISH, "Lagos Urban", "Welcome to DIV AI, where your imagination comes alive."),
        VoiceProfile("v_chidi", "Chidi", "Male", "Adult", VoiceLanguage.NIGERIAN_ENGLISH, "Enugu Warm", "Every great story deserves vibrant animation and sound."),
        VoiceProfile("v_kemi", "Kemi", "Female", "Youthful", VoiceLanguage.PIDGIN_ENGLISH, "Pidgin Energy", "No worry yourself! DIV AI go turn your script to film in minutes!"),
        VoiceProfile("v_emeka", "Emeka", "Male", "Heroic", VoiceLanguage.PIDGIN_ENGLISH, "Streetwise Pidgin", "Make we show the world wetin African creatives fit do!"),
        VoiceProfile("v_adaeze", "Adaeze", "Female", "Storyteller", VoiceLanguage.IGBO, "Traditional Igbo", "Nno nu! Ka anyi koro akuko mara mma site na DIV AI."),
        VoiceProfile("v_adebayo", "Adebayo", "Male", "Warm Baritone", VoiceLanguage.YORUBA, "Cultural Yoruba", "E kaabo! E je ki a fi ogbon AI se ere orin ati itan."),
        VoiceProfile("v_ibrahim", "Ibrahim", "Male", "Resolute", VoiceLanguage.HAUSA, "Northern Hausa", "Barka da zuwa! Bari mu kirkiri labarai masu kayatarwa."),
        VoiceProfile("v_sarah", "Sarah", "Female", "Professional", VoiceLanguage.ENGLISH, "Standard Studio", "DIV AI empowers creators to render cinema-grade animations effortlessly.")
    )
}
