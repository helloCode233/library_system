package com.library.common;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TF-IDF 文本相似度工具类
 * 对中文文本使用字符二元组（bigram）分词，对英文使用空格分词
 */
public class TfidfUtil {

    /**
     * 对文本进行分词
     * - 中文：连续汉字提取2字符滑动窗口（bigram）
     * - 英文：按空格拆分单词，保留长度>=2的纯字母单词并转小写
     * - 过滤标点符号和空白
     */
    public static List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        List<String> tokens = new ArrayList<>();
        String cleaned = text.replaceAll("[\\p{P}\\p{S}]", " ").trim();
        if (cleaned.isEmpty()) return tokens;
        String[] words = cleaned.split("\\s+");
        for (String word : words) {
            if (word.length() >= 2 && word.matches("[a-zA-Z]+")) {
                tokens.add(word.toLowerCase());
            }
        }
        StringBuilder chineseSeq = new StringBuilder();
        for (char c : cleaned.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fff') {
                chineseSeq.append(c);
            }
        }
        String chinese = chineseSeq.toString();
        for (int i = 0; i < chinese.length() - 1; i++) {
            tokens.add(chinese.substring(i, i + 2));
        }
        return tokens;
    }

    /**
     * 计算单个文档的 TF（词频）向量
     * TF = 词在文档中出现的次数 / 文档总词数
     */
    public static Map<String, Double> computeTf(List<String> tokens) {
        Map<String, Double> tf = new HashMap<>();
        if (tokens.isEmpty()) return tf;
        Map<String, Long> counts = tokens.stream()
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        double total = tokens.size();
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            tf.put(entry.getKey(), entry.getValue() / total);
        }
        return tf;
    }

    /**
     * 为所有文档计算 TF-IDF 向量
     * @param docs 文档映射：文档ID -> 分词结果
     * @return 文档ID -> (词 -> TF-IDF分数)
     */
    public static Map<String, Map<String, Double>> computeTfidfVectors(Map<String, List<String>> docs) {
        List<Map.Entry<String, Map<String, Double>>> tfList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : docs.entrySet()) {
            Map<String, Double> tf = computeTf(entry.getValue());
            tfList.add(new AbstractMap.SimpleEntry<>(entry.getKey(), tf));
        }
        int N = tfList.size();
        Map<String, Integer> docFreq = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> docEntry : tfList) {
            for (String term : docEntry.getValue().keySet()) {
                docFreq.merge(term, 1, Integer::sum);
            }
        }
        Map<String, Double> idf = new HashMap<>();
        for (Map.Entry<String, Integer> entry : docFreq.entrySet()) {
            idf.put(entry.getKey(), Math.log((double) N / entry.getValue()));
        }
        Map<String, Map<String, Double>> tfidf = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Double>> docEntry : tfList) {
            Map<String, Double> vec = new HashMap<>();
            for (Map.Entry<String, Double> termEntry : docEntry.getValue().entrySet()) {
                String term = termEntry.getKey();
                double score = termEntry.getValue() * idf.getOrDefault(term, 0.0);
                vec.put(term, score);
            }
            tfidf.put(docEntry.getKey(), vec);
        }
        return tfidf;
    }

    /**
     * 计算两个 TF-IDF 向量之间的余弦相似度
     */
    public static double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        if (v1.isEmpty() || v2.isEmpty()) return 0.0;
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (double val : v1.values()) norm1 += val * val;
        norm1 = Math.sqrt(norm1);
        for (double val : v2.values()) norm2 += val * val;
        norm2 = Math.sqrt(norm2);
        if (norm1 == 0.0 || norm2 == 0.0) return 0.0;
        Map<String, Double> smaller = v1.size() <= v2.size() ? v1 : v2;
        Map<String, Double> larger = v1.size() <= v2.size() ? v2 : v1;
        for (Map.Entry<String, Double> entry : smaller.entrySet()) {
            Double val2 = larger.get(entry.getKey());
            if (val2 != null) {
                dotProduct += entry.getValue() * val2;
            }
        }
        return dotProduct / (norm1 * norm2);
    }

    /**
     * 搜索：将 query 分词后计算 TF 向量，与预计算的文档 TF-IDF 向量做余弦相似度，
     * 返回按相似度降序排列的文档 ID 列表（只返回相似度 > 0 的结果）
     */
    public static List<Map.Entry<String, Double>> search(String query, Map<String, Map<String, Double>> docVectors) {
        List<String> queryTokens = tokenize(query);
        if (queryTokens.isEmpty()) return Collections.emptyList();
        Map<String, Double> queryVec = computeTf(queryTokens);
        List<Map.Entry<String, Double>> results = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> docEntry : docVectors.entrySet()) {
            double sim = cosineSimilarity(queryVec, docEntry.getValue());
            if (sim > 0) {
                results.add(new AbstractMap.SimpleEntry<>(docEntry.getKey(), sim));
            }
        }
        results.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        return results;
    }
}
