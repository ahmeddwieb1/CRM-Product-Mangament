//package org.elmorshedy.AI;
//
//import org.elmorshedy.note.models.Gender;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class AiService {
//    private final WebClient webClient;
//    private final String apiKey;
//
//    @Autowired
//    public AiService(WebClient geminiWebClient, String geminiApiKey) {
//        this.webClient = geminiWebClient;
//        this.apiKey = geminiApiKey;
//    }
//
//    public String getAiReply(String content, boolean isFamiliar, Gender gender) {
//        String style;
//
//        if (isFamiliar) {
//            if (Gender.MALE.equals(gender)) {
//                style = "انت رد علي رسايل لموقع والرسايل اللي هتجيلك عباره عن تعليقات اكتب رد ودود وشخصي زي ما بيكون اللي بيتكلم مع صاحبه ورد بالهجه المصريه خلي الرد اخره 6 كلمات وخاطب العميل بصيغة المذكر للرسالة التالية:";
//            } else if (Gender.FEMALE.equals(gender)) {
//                style = "انت رد علي رسايل لموقع والرسايل اللي هتجيلك عباره عن تعليقات اكتب رد ودود وشخصي زي ما بيكون اللي بيتكلم مع صاحبته ورد بالهجه المصريه خلي الرد اخره 6 كلمات وخاطب العميل بصيغة المؤنث للرسالة التالية:";
//            } else {
//                style = "انت رد علي رسايل لموقع والرسايل اللي هتجيلك عباره عن تعليقات اكتب رد ودود وشخصي ورد بالهجه المصريه خلي الرد اخره 6 كلمات للرسالة التالية:";
//            }
//        } else {
//            if (Gender.MALE.equals(gender)) {
//                style = "اكتب رد رسمي وبسيط بالهجه المصريه وخاطب العميل بصيغة المذكر للرسالة التالية:";
//            } else if (Gender.FEMALE.equals(gender)) {
//                style = "اكتب رد رسمي وبسيط بالهجه المصريه وخاطب العميل بصيغة المؤنث للرسالة التالية:";
//            } else {
//                style = "اكتب رد رسمي وبسيط بالهجه المصريه للرسالة التالية:";
//            }
//        }
//
//        // بناء جسم الطلب لـ Gemini API
//        Map<String, Object> requestBody = Map.of(
//                "contents", List.of(
//                        Map.of("parts", List.of(
//                                Map.of("text", style + " " + content)
//                        ))
//                ),
//                "generationConfig", Map.of(
//                        "maxOutputTokens", 50, // تقليل عدد الكلمات للإخراج
//                        "temperature", 0.7 // درجة الإبداع (0-1)
//                )
//        );
//
//        try {
//            Map<String, Object> response = webClient.post()
//                    .uri("/models/gemini-1.5-flash:generateContent?key=" + apiKey)
//                    .bodyValue(requestBody)
//                    .retrieve()
//                    .bodyToMono(Map.class)
//                    .block();
//
//            // استخراج النص من response
//            if (response != null && response.containsKey("candidates")) {
//                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
//                if (candidates != null && !candidates.isEmpty()) {
//                    Map<String, Object> candidate = candidates.get(0);
//                    if (candidate.containsKey("content")) {
//                        Map<String, Object> contentMap = (Map<String, Object>) candidate.get("content");
//                        if (contentMap.containsKey("parts")) {
//                            List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
//                            if (parts != null && !parts.isEmpty() && parts.get(0).containsKey("text")) {
//                                return (String) parts.get(0).get("text");
//                            }
//                        }
//                    }
//                }
//            }
//
//            return "لم أستطع توليد رد، يرجى المحاولة مرة أخرى.";
//
//        } catch (Exception e) {
//            // تسجيل الخطأ لل debugging
//            System.err.println("Gemini API Error: " + e.getMessage());
//            return "عذراً، حدث خطأ في توليد الرد. يرجى المحاولة لاحقاً.";
//        }
//    }
//}