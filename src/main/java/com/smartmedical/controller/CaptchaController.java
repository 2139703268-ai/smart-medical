package com.smartmedical.controller;

import com.smartmedical.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class CaptchaController {

    private static final ConcurrentHashMap<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();
    private static final long EXPIRE_MS = 5 * 60 * 1000;
    private static final Random random = new Random();
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int CAPTCHA_LENGTH = 4;

    static {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            captchaStore.entrySet().removeIf(e -> now - e.getValue().createTime > EXPIRE_MS);
        }, 1, 1, TimeUnit.MINUTES);
    }

    @GetMapping("/captcha")
    public Result<Map<String, String>> generateCaptcha() throws IOException {
        String code = generateCode();
        String key = UUID.randomUUID().toString().replace("-", "");
        captchaStore.put(key, new CaptchaEntry(code.toLowerCase(), System.currentTimeMillis()));

        String base64Image = generateCaptchaImage(code);

        return Result.success(Map.of(
                "captchaKey", key,
                "captchaImage", "data:image/png;base64," + base64Image
        ));
    }

    public static boolean verifyCaptcha(String key, String code) {
        if (key == null || code == null) return false;
        CaptchaEntry entry = captchaStore.remove(key);
        if (entry == null) return false;
        if (System.currentTimeMillis() - entry.createTime > EXPIRE_MS) return false;
        return code.trim().toLowerCase().equals(entry.answer);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CAPTCHA_LENGTH);
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            sb.append(CAPTCHA_CHARS.charAt(random.nextInt(CAPTCHA_CHARS.length())));
        }
        return sb.toString();
    }

    private String generateCaptchaImage(String code) throws IOException {
        int width = 120, height = 40;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        for (int i = 0; i < 30; i++) {
            int x1 = random.nextInt(width), y1 = random.nextInt(height);
            int x2 = random.nextInt(width), y2 = random.nextInt(height);
            g.setColor(randomColor(150, 200));
            g.drawLine(x1, y1, x2, y2);
        }

        int fontSize = 28;
        g.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(randomColor(20, 130));
            int x = 8 + i * 26;
            int y = 28 + random.nextInt(6) - 3;
            int angle = random.nextInt(30) - 15;
            g.rotate(Math.toRadians(angle), x + 10, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-Math.toRadians(angle), x + 10, y);
        }

        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private Color randomColor(int min, int max) {
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }

    private static class CaptchaEntry {
        String answer;
        long createTime;
        CaptchaEntry(String answer, long createTime) {
            this.answer = answer;
            this.createTime = createTime;
        }
    }
}
