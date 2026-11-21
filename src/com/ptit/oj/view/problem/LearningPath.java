package com.ptit.oj.view.problem;

import com.ptit.oj.model.Problem;
import com.ptit.oj.model.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LearningPath {
    public static final String OTHER_TOPIC = "__other__";

    public static List<Problem> orderByPath(List<Problem> problems, List<Topic> topics) {
        List<Problem> sorted = new ArrayList<>(problems);
        Collections.sort(sorted, new Comparator<Problem>() {
            @Override
            public int compare(Problem a, Problem b) {
                int rankA = topicRank(a, topics);
                int rankB = topicRank(b, topics);
                if (rankA != rankB) {
                    return rankA - rankB;
                }
                int orderA = orderOf(a);
                int orderB = orderOf(b);
                if (orderA != orderB) {
                    return Integer.compare(orderA, orderB);
                }
                return a.getId().compareTo(b.getId());
            }
        });
        return sorted;
    }

    private static int topicRank(Problem problem, List<Topic> topics) {
        for (int i = 0; i < topics.size(); i++) {
            if (topics.get(i).getId().equals(problem.getTopic())) return i;
        }
        return topics.size();
    }

    private static int orderOf(Problem problem) {
        if (problem.getOrder() > 0) return problem.getOrder();
        return Integer.MAX_VALUE;
    }

    public static String topicKey(Problem problem, List<Topic> topics) {
        for (Topic t : topics) {
            if (t.getId().equals(problem.getTopic())) return t.getId();
        }
        return OTHER_TOPIC;
    }

    public static String topicName(Problem problem, List<Topic> topics) {
        for (Topic t : topics) {
            if (t.getId().equals(problem.getTopic())) return t.getName();
        }
        return "Bài khác";
    }
}
