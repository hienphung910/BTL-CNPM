package com.ptit.oj.dao;

import com.ptit.oj.model.Submission;

import java.util.List;

public interface SubmissionRepository extends Repository<Submission> {
    void saveWithResults(Submission submission);

    List<Submission> findAllNewestFirst();

    List<Submission> findByUserId(String userId);
}
