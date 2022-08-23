package com.example.mydrivingapp.model;

public class Case {
    public int wordCnt;
    public int questionCnt;
    public int delayToSpeak;
    public int delayDuringSpeak;

    public Case(int wordCnt, int questionCnt, int delayToSpeak, int delayDuringSpeak) {
        this.wordCnt = wordCnt;
        this.questionCnt = questionCnt;
        this.delayToSpeak = delayToSpeak;
        this.delayDuringSpeak = delayDuringSpeak;
    }

    public int getWordCnt() {
        return wordCnt;
    }

    public void setWordCnt(int wordCnt) {
        this.wordCnt = wordCnt;
    }

    public int getQuestionCnt() {
        return questionCnt;
    }

    public void setQuestionCnt(int questionCnt) {
        this.questionCnt = questionCnt;
    }

    public int getDelayToSpeak() {
        return delayToSpeak;
    }

    public void setDelayToSpeak(int delayToSpeak) {
        this.delayToSpeak = delayToSpeak;
    }

    public int getDelayDuringSpeak() {
        return delayDuringSpeak;
    }

    public void setDelayDuringSpeak(int delayDuringSpeak) {
        this.delayDuringSpeak = delayDuringSpeak;
    }
}
