package com.lzhphantom.lzhphantom_findfriendsbackend.utls;

import cn.hutool.core.collection.CollUtil;

import java.util.Set;

public class AlgorithmUtils {
    // 计算两个字符串之间的编辑距离
    public static int computeEditDistance(String str1, String str2) {
        int len1 = str1.length();
        int len2 = str2.length();

        // 创建一个二维数组来存储子问题的结果
        int[][] dp = new int[len1 + 1][len2 + 1];

        // 初始化边界条件
        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                if (i == 0) {
                    dp[i][j] = j; // 当str1为空时，需要进行j次插入操作
                } else if (j == 0) {
                    dp[i][j] = i; // 当str2为空时，需要进行i次删除操作
                }
            }
        }

        // 填充dp数组
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1]; // 如果最后一个字符相同，则不需要额外的操作
                } else {
                    dp[i][j] = 1 + Math.min(
                            Math.min(dp[i - 1][j], // 删除
                                    dp[i][j - 1]), // 插入
                            dp[i - 1][j - 1]); // 替换
                }
            }
        }

        // 返回最终的编辑距离
        return dp[len1][len2];
    }

    // 计算两个用户之间的Jaccard相似度
    public static double computeJaccardSimilarity(Set<String> set1, Set<String> set2) {
        // 创建交集
        Set<String> intersection = CollUtil.newHashSet(set1);
        intersection.retainAll(set2);

        // 创建并集
        Set<String> union = CollUtil.newHashSet(set1);
        union.addAll(set2);

        // 如果并集为空，返回1.0表示完全相同（避免除以零）
        if (union.isEmpty()) {
            return 1.0;
        }

        // 返回Jaccard相似度
        return (double) intersection.size() / union.size();
    }
}
