package com.chwipoClova.qa.service;

import com.chwipoClova.common.exception.CommonException;
import com.chwipoClova.common.exception.ExceptionCode;
import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.common.response.MessageCode;
import com.chwipoClova.common.utils.ApiUtils;
import com.chwipoClova.feedback.request.FeedbackInsertReq;
import com.chwipoClova.feedback.service.FeedbackService;
import com.chwipoClova.interview.entity.Interview;
import com.chwipoClova.interview.entity.InterviewEditor;
import com.chwipoClova.interview.repository.InterviewRepository;
import com.chwipoClova.qa.entity.Qa;
import com.chwipoClova.qa.entity.QaEditor;
import com.chwipoClova.qa.repository.QaRepository;
import com.chwipoClova.qa.request.QaAnswerDataInsertReq;
import com.chwipoClova.qa.request.QaAnswerInsertReq;
import com.chwipoClova.qa.request.QaGenerateReq;
import com.chwipoClova.qa.request.QaQuestionInsertReq;
import com.chwipoClova.qa.response.QaCountRes;
import com.chwipoClova.qa.response.QaListForFeedbackRes;
import com.chwipoClova.qa.response.QaListRes;
import com.chwipoClova.qa.response.QaQuestionInsertRes;
import com.chwipoClova.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Service
@Slf4j
public class QaService {

    private final QaRepository qaRepository;

    private final InterviewRepository interviewRepository;

    private final FeedbackService feedbackService;

    private final UserRepository userRepository;

    private final ApiUtils apiUtils;

    @Transactional
    public List<QaQuestionInsertRes> insertQaQuestionList(List<QaQuestionInsertReq> qaQuestionInsertReqList) throws IOException {
        List<Qa> qaList = new ArrayList<>();
        qaQuestionInsertReqList.stream().forEach(qaQuestionInsertReq -> {
            Qa qa = Qa.builder()
                    .question(qaQuestionInsertReq.getQuestion())
                    .aiAnswer(qaQuestionInsertReq.getAiAnswer())
                    .interview(qaQuestionInsertReq.getInterview())
                    .build();
            qaList.add(qa);
        });
        List<Qa> qaListRst = qaRepository.saveAll(qaList);

        List<QaQuestionInsertRes> qaQuestionInsertResList = new ArrayList<>();

        qaListRst.stream().forEach(qa -> {
            QaQuestionInsertRes qaQuestionInsertRes = QaQuestionInsertRes.builder()
                    .qaId(qa.getQaId())
                    .question(qa.getQuestion())
                    .interviewId(qa.getInterview().getInterviewId())
                    .regDate(qa.getRegDate())
                    .modifyDate(qa.getModifyDate())
                    .build();
            qaQuestionInsertResList.add(qaQuestionInsertRes);
        });

        return qaQuestionInsertResList;
    }

    @Transactional
    public CommonResponse insertAnswer(QaAnswerInsertReq qaAnswerInsertReq) throws IOException {
        Long userId = qaAnswerInsertReq.getUserId();
        Long interviewId = qaAnswerInsertReq.getInterviewId();
        Interview interview = interviewRepository.findByUserUserIdAndInterviewId(userId, interviewId).orElseThrow(() -> new CommonException(ExceptionCode.INTERVIEW_NULL.getMessage(), ExceptionCode.INTERVIEW_NULL.getCode()));

        List<QaAnswerDataInsertReq> qaAnswerDataInsertReqList = qaAnswerInsertReq.getAnswerData();

        List<FeedbackInsertReq> feedbackInsertListReq = new ArrayList<>();

        Qa lastQa = qaRepository.findFirstByInterviewInterviewIdOrderByQaIdDesc(interviewId);
        Long lastQaId = lastQa.getQaId();

        StringBuilder questionStringBuilder = new StringBuilder();
        StringBuilder answerStringBuilder = new StringBuilder();
        AtomicBoolean lastCkAtomic = new AtomicBoolean(false);

        AtomicLong answerCnt = new AtomicLong();
        qaAnswerDataInsertReqList.stream().forEach(qaAnswerDataInsertReq -> {
            String answer = qaAnswerDataInsertReq.getAnswer();

            // 답변 내용이 있을 경우 답변 저장 및 피드백 생성
            if (StringUtils.isNotBlank(answer)) {
                Qa qa = qaRepository.findByInterviewInterviewIdAndQaId(interviewId, qaAnswerDataInsertReq.getQaId()).orElseThrow(() -> new CommonException(ExceptionCode.QA_NULL.getMessage(), ExceptionCode.QA_NULL.getCode()));
                QaEditor.QaEditorBuilder editorBuilder = qa.toEditor();
                QaEditor qaEditor = editorBuilder.answer(qaAnswerDataInsertReq.getAnswer())
                        .build();
                qa.edit(qaEditor);

                if (lastQaId == qa.getQaId()) {
                    lastCkAtomic.set(true);
                }

                answerCnt.getAndIncrement();

                questionStringBuilder.append(answerCnt.get() + ". " + qa.getQuestion());
                questionStringBuilder.append("\n");

                answerStringBuilder.append(answerCnt.get() + ". " + qa.getAnswer());
                answerStringBuilder.append("\n");


                // 피드백 정보
                FeedbackInsertReq feedbackInsertReq = new FeedbackInsertReq();
                feedbackInsertReq.setQaId(qa.getQaId());
                feedbackInsertReq.setAnswer(qa.getAnswer());
                feedbackInsertReq.setQuestion(qa.getQuestion());
                feedbackInsertReq.setApiNum(answerCnt.get());
                feedbackInsertListReq.add(feedbackInsertReq);
            }
        });

        // 마지막 답변이 있을 경우 면접 완료 처리 및 피드백 생성
        Boolean lastCk = lastCkAtomic.get();

        // 마지막 문제 넘기기 눌렀을 경우
        Integer lastBtnCk = qaAnswerInsertReq.getLastBtnCk() == null ? 0 : qaAnswerInsertReq.getLastBtnCk();

        if (lastCk || lastBtnCk == 1) {

            String allQuestionData = questionStringBuilder.toString().trim();
            String allAnswerData = answerStringBuilder.toString().trim();

            // 면접관의 속마음
            String apiFeelRst = apiUtils.feel(allAnswerData);

            feedbackService.insertFeedback(allQuestionData, allAnswerData, feedbackInsertListReq);

            // 면접 완료 처리
            InterviewEditor.InterviewEditorBuilder editorBuilder = interview.toEditor();
            InterviewEditor interviewEditor = editorBuilder.status(1).feedback(apiFeelRst).build();
            interview.edit(interviewEditor);
        }

        return new CommonResponse<>(MessageCode.OK.getCode(), null, MessageCode.OK.getMessage());
    }



    public List<QaListRes> selectQaList(Long interviewId) {
        List<QaListRes> qaListResList = new ArrayList<>();

        List<Qa> qaList = qaRepository.findByInterviewInterviewIdOrderByQaId(interviewId);

        qaList.stream().forEach(qa -> {
            QaListRes qaListRes = QaListRes.builder()
                    .interviewId(qa.getInterview().getInterviewId())
                    .qaId(qa.getQaId())
                    .question(qa.getQuestion())
                    .answer(qa.getAnswer())
                    .regDate(qa.getRegDate())
                    .modifyDate(qa.getModifyDate())
                    .build();
            qaListResList.add(qaListRes);
        });
        return qaListResList;
    }

    public List<QaListForFeedbackRes> selectQaListForFeedback(Long interviewId) {
        List<QaListRes> qaListRes = selectQaList(interviewId);
        List<QaListForFeedbackRes> listForFeedbackResList = new ArrayList<>();

        AtomicReference<List<String>> feedback1 = new AtomicReference<>(new ArrayList<>());
        qaListRes.stream().forEach(qaListRes1 -> {
            AtomicReference<String> feedback2 = new AtomicReference<>("");

            feedbackService.selectFeedbackList(qaListRes1.getQaId()).stream().forEach(feedbackListRes -> {
                Integer type = feedbackListRes.getType();
                if (type == 1) {
                    // 키워드는 ,로 분리
                    String keyword = feedbackListRes.getContent();
                    String[] keywordArray = keyword.split(",");
                    AtomicInteger atomicInteger = new AtomicInteger();

                    for (String key : keywordArray) {
                        atomicInteger.incrementAndGet();
                        Integer keywordCnt = atomicInteger.get();
                        if (keywordCnt < 5) {
                            addElement(feedback1, key);
                        }
                    }
                } else if (type == 2) {
                    feedback2.set(feedbackListRes.getContent());
                }
            });

            QaListForFeedbackRes qalistForFeedbackRes = QaListForFeedbackRes.builder()
                    .interviewId(qaListRes1.getInterviewId())
                    .qaId(qaListRes1.getQaId())
                    .question(qaListRes1.getQuestion())
                    .answer(qaListRes1.getAnswer())
                    .regDate(qaListRes1.getRegDate())
                    .modifyDate(qaListRes1.getModifyDate())
                    .keyword(feedback1.get())
                    .bestAnswer(feedback2.get())
                    .build();
            listForFeedbackResList.add(qalistForFeedbackRes);
            feedback1.set(new ArrayList<>());
        });
        return listForFeedbackResList;
    }

    public QaCountRes selectQaListUseCount(Long interviewId) {
        List<QaListRes> qaListRes = selectQaList(interviewId);

        AtomicInteger index = new AtomicInteger(0); // 시작 인덱스
        AtomicReference<Long> lastQaIdAtomic = new AtomicReference<>();
        qaListRes.stream().forEach(qaListRes1 -> {
            if (StringUtils.isNotBlank(qaListRes1.getAnswer())) {
                index.getAndIncrement();
                lastQaIdAtomic.set(qaListRes1.getQaId());
            }
        });

        int useCnt = index.get();
        int totalCnt = qaListRes.size();
        Long lastQaId = lastQaIdAtomic.get();

        return QaCountRes.builder()
                .useCnt(useCnt)
                .totalCnt(totalCnt)
                .lastQaId(lastQaId)
                .build();
    }

    @Transactional
    public void initQa(Long interviewId) {
        qaRepository.initQa(interviewId);
    }

    public void deleteQa(Long interviewId) {
        qaRepository.deleteByInterviewInterviewId(interviewId);
    }

    public void generateQa(QaGenerateReq qaGenerateReq) throws IOException {
        Long interviewId = qaGenerateReq.getInterviewId();
        Long userId = qaGenerateReq.getUserId();

        userRepository.findById(userId).orElseThrow(() -> new CommonException(ExceptionCode.USER_NULL.getMessage(), ExceptionCode.USER_NULL.getCode()));
        Interview interview = interviewRepository.findByUserUserIdAndInterviewId(userId, interviewId).orElseThrow(() -> new CommonException(ExceptionCode.INTERVIEW_NULL.getMessage(), ExceptionCode.INTERVIEW_NULL.getCode()));
        Integer status = interview.getStatus();

        if (status != 0) {
            throw new CommonException(ExceptionCode.INTERVIEW_COMPLETE.getMessage(), ExceptionCode.INTERVIEW_COMPLETE.getCode());
        }

        List<Qa> qaList = qaRepository.findByInterviewInterviewIdOrderByQaId(interviewId);

        // 피드백 데이터가 있는지 확인 있으면 오류 발생
        qaList.stream().forEach(qa -> {
            int feedbackSize = feedbackService.selectFeedbackList(qa.getQaId()).size();
            if (feedbackSize > 0) {
                throw new CommonException(ExceptionCode.FEEDBACK_NOT_NULL.getMessage(), ExceptionCode.FEEDBACK_NOT_NULL.getCode());
            }
        });

        // 기존 질문 전부 삭제
        qaRepository.deleteAll(qaList);

        // 새로운 질문 생성
        insertQa(interview);
    }

    public List<QaQuestionInsertRes> insertQa(Interview interviewRst) throws IOException {
        String recruitSummary = interviewRst.getRecruitSummary();
        String resumeSummary = interviewRst.getResumeSummary();

        String apiQaRst = apiUtils.question(recruitSummary, resumeSummary);

        List<QaQuestionInsertReq> qaQuestionInsertReqList = new ArrayList<>();
        getApiQaList(apiQaRst).stream().forEach(apiQa ->  {
            QaQuestionInsertReq qaQuestionInsertReq = new QaQuestionInsertReq();
            qaQuestionInsertReq.setInterview(interviewRst);
            qaQuestionInsertReq.setQuestion(apiQa);
            qaQuestionInsertReqList.add(qaQuestionInsertReq);
        });
        return insertQaQuestionList(qaQuestionInsertReqList);
    }

    private List<String> getApiQaList(String apiQaRst) {
        // 현재 사용하지 않는 --- 제거 및 줄바꿈 분리
        String[] splitSummaryList = apiQaRst.split("\n");

        List<String> apiQaList = new ArrayList<>();
        for (String splitSummary : splitSummaryList) {
            if (splitSummary.indexOf(".") != -1) {
                String num = splitSummary.substring(0, splitSummary.indexOf("."));
                if (org.apache.commons.lang3.StringUtils.isNumeric(num)) {
                    apiQaList.add(splitSummary.trim());
                }
            }
        }

        if (apiQaList.size() == 0) {
            throw new CommonException(ExceptionCode.API_QA_NULL.getMessage(), ExceptionCode.API_QA_NULL.getCode());
        }

        return apiQaList;
    }

    // 원자적으로 리스트에 요소를 추가하는 메소드
    private static void addElement(AtomicReference<List<String>> atomicListRef, String element) {
        List<String> oldList;
        List<String> newList;

        do {
            oldList = atomicListRef.get();
            newList = new ArrayList<>(oldList);
            newList.add(element);
        } while (!atomicListRef.compareAndSet(oldList, newList));
    }
}
