#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec2 u_resolution;
uniform vec2 u_texelSize;
uniform float u_time;
uniform float u_glowStrength;
uniform float u_glowRadius;
uniform float u_scanlineStrength;
uniform vec4 u_tint;

vec4 spriteSample(vec2 uv) {
    return texture2D(u_texture, uv);
}

float maxAlphaAround(vec2 uv, float radius) {
    vec2 stepSize = u_texelSize * radius;
    float alpha = 0.0;
    alpha = max(alpha, spriteSample(uv + vec2(stepSize.x, 0.0)).a);
    alpha = max(alpha, spriteSample(uv - vec2(stepSize.x, 0.0)).a);
    alpha = max(alpha, spriteSample(uv + vec2(0.0, stepSize.y)).a);
    alpha = max(alpha, spriteSample(uv - vec2(0.0, stepSize.y)).a);
    alpha = max(alpha, spriteSample(uv + vec2(stepSize.x, stepSize.y)).a);
    alpha = max(alpha, spriteSample(uv + vec2(-stepSize.x, stepSize.y)).a);
    alpha = max(alpha, spriteSample(uv + vec2(stepSize.x, -stepSize.y)).a);
    alpha = max(alpha, spriteSample(uv - vec2(stepSize.x, stepSize.y)).a);
    return alpha;
}

void main() {
    vec4 source = spriteSample(v_texCoords) * v_color;
    float radius = clamp(u_glowRadius, 0.0, 8.0);
    float outerAlpha = maxAlphaAround(v_texCoords, max(radius, 1.0));

    float edgeGlow = max(0.0, outerAlpha - source.a);
    float pulse = 0.88 + 0.12 * sin(u_time * 5.0);
    vec3 glowColor = mix(u_tint.rgb, vec3(1.0), 0.22);
    vec3 rgb = source.rgb + glowColor * edgeGlow * u_glowStrength * pulse;

    float y = gl_FragCoord.y / max(u_resolution.y, 1.0);
    float scanline = 0.5 + 0.5 * sin(y * u_resolution.y * 0.20);
    float scanlineMix = clamp(u_scanlineStrength, 0.0, 1.0) * 0.14;
    rgb *= 1.0 - scanline * scanlineMix;

    float alpha = max(source.a, edgeGlow * u_glowStrength * 0.68);
    gl_FragColor = vec4(rgb, clamp(alpha, 0.0, 1.0));
}
