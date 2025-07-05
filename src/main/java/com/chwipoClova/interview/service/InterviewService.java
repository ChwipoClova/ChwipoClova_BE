package com.chwipoClova.interview.service;

import com.chwipoClova.common.enums.CommonCode;
import com.chwipoClova.common.exception.CommonException;
import com.chwipoClova.common.exception.ExceptionCode;
import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.common.response.MessageCode;
import com.chwipoClova.feedback.request.FeedbackGenerateReq;
import com.chwipoClova.feedback.service.FeedbackService;
import com.chwipoClova.interview.entity.Interview;
import com.chwipoClova.interview.entity.InterviewEditor;
import com.chwipoClova.interview.repository.InterviewRepository;
import com.chwipoClova.interview.request.InterviewDeleteReq;
import com.chwipoClova.interview.request.InterviewInitQaReq;
import com.chwipoClova.interview.request.InterviewInsertReq;
import com.chwipoClova.interview.response.InterviewInsertRes;
import com.chwipoClova.interview.response.InterviewListRes;
import com.chwipoClova.interview.response.InterviewQaListRes;
import com.chwipoClova.interview.response.InterviewRes;
import com.chwipoClova.qa.entity.Qa;
import com.chwipoClova.qa.request.QaAnswerInsertReq;
import com.chwipoClova.qa.request.QaGenerateReq;
import com.chwipoClova.qa.response.QaCountRes;
import com.chwipoClova.qa.response.QaListForFeedbackRes;
import com.chwipoClova.qa.response.QaListRes;
import com.chwipoClova.qa.response.QaQuestionInsertRes;
import com.chwipoClova.qa.service.QaService;
import com.chwipoClova.recruit.request.RecruitInsertReq;
import com.chwipoClova.recruit.response.RecruitInsertRes;
import com.chwipoClova.recruit.service.RecruitService;
import com.chwipoClova.resume.entity.Resume;
import com.chwipoClova.resume.service.ResumeService;
import com.chwipoClova.user.entity.User;
import com.chwipoClova.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class InterviewService {

    private final ResumeService resumeService;

    private final UserRepository userRepository;

    private final RecruitService recruitService;

    private final InterviewRepository interviewRepository;

    private final QaService qaService;

    private final FeedbackService feedbackService;

    @Value("${limit.size.interview}")
    private Integer interviewLimitSize;

    @Transactional
    public InterviewInsertRes insertInterview(InterviewInsertReq interviewInsertReq, MultipartFile file) throws Exception {
        Long userId = interviewInsertReq.getUserId();
        Long resumeId = interviewInsertReq.getResumeId();
        String recruitContent = interviewInsertReq.getRecruitContent();

        User user = userRepository.findById(userId).orElseThrow(() -> new CommonException(ExceptionCode.USER_NULL.getMessage(), ExceptionCode.USER_NULL.getCode()));
        Resume resume = resumeService.findByUserUserIdAndResumeIdAndDelFlag(userId, resumeId).orElseThrow(() -> new CommonException(ExceptionCode.RESUME_NULL.getMessage(), ExceptionCode.RESUME_NULL.getCode()));

/*        List<Interview> interviewList = interviewRepository.findByUserUserIdOrderByRegDate(userId);
        if (interviewList != null && interviewList.size() >= interviewLimitSize) {
            throw new CommonException(ExceptionCode.INTERVIEW_LIST_OVER.getMessage(), ExceptionCode.INTERVIEW_LIST_OVER.getCode());
        }*/

        // 채용 공고 등록 및 조회
        RecruitInsertReq recruitInsertReq = new RecruitInsertReq();
        recruitInsertReq.setUserId(userId);
        recruitInsertReq.setRecruitContent(recruitContent);
        recruitInsertReq.setFile(file);
        RecruitInsertRes recruitInsertRes = recruitService.insertRecruit(recruitInsertReq);

        String title = recruitInsertRes.getTitle();
        //String recruitSummary = recruitInsertRes.getSummary();
        //String resumeSummary = resume.getSummary();

        String recruitSummary = recruitInsertRes.getOriginText();
        String resumeSummary = resume.getOriginText();

        // 면접 저장
        Interview interview = Interview.builder()
                .title(title)
                .recruitSummary(recruitSummary)
                .resumeSummary(resumeSummary)
                .user(user)
                .build();
        Interview interviewRst = interviewRepository.save(interview);

        List<QaQuestionInsertRes> questionData = qaService.insertQa(interviewRst);

        return InterviewInsertRes.builder()
                .interviewId(interviewRst.getInterviewId())
                .userId(userId)
                .title(interviewRst.getTitle())
                .regDate(interviewRst.getRegDate())
                .questionData(questionData)
                .build();
    }

    public InterviewRes selectInterview(Long userId, Long interviewId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CommonException(ExceptionCode.USER_NULL.getMessage(), ExceptionCode.USER_NULL.getCode()));
        Interview interview = findByUserUserIdAndInterviewIdAndDelFlag(userId, interviewId).orElseThrow(() -> new CommonException(ExceptionCode.INTERVIEW_NULL.getMessage(), ExceptionCode.INTERVIEW_NULL.getCode()));

        List<QaListForFeedbackRes> listForFeedbackResList = qaService.selectQaListForFeedback(interview.getInterviewId());

        return InterviewRes.builder()
                .interviewId(interview.getInterviewId())
                .userId(user.getUserId())
                .title(interview.getTitle())
                .status(interview.getStatus())
                .interviewerFeel(interview.getFeedback())
                .regDate(interview.getRegDate())
                .qaData(listForFeedbackResList)
                .build();
    }

    public Optional<Interview> findByUserUserIdAndInterviewIdAndDelFlag(Long userId, Long interviewId) {
        return interviewRepository.findByUserUserIdAndInterviewIdAndDelFlag(userId, interviewId, CommonCode.DELETE_N.getCode());
    }

    public List<InterviewListRes> selectInterviewList(Long userId) {
        List<InterviewListRes> interviewListRes = new ArrayList<>();

        List<Interview> interviewList = findByUserUserIdOrderByRegDate(userId);

        interviewList.stream().forEach(interview -> {
            QaCountRes qaCountRes = qaService.selectQaListUseCount(interview.getInterviewId());

            InterviewListRes interviewListRes1 = InterviewListRes.builder()
                    .interviewId(interview.getInterviewId())
                    .userId(interview.getUser().getUserId())
                    .title(interview.getTitle())
                    .useCnt(qaCountRes.getUseCnt())
                    .totalCnt(qaCountRes.getTotalCnt())
                    .status(interview.getStatus())
                    .regDate(interview.getRegDate())
                    .build();
            interviewListRes.add(interviewListRes1);
        });

        return interviewListRes;
    }

    public List<Interview> findByUserUserIdOrderByRegDate(Long userId) {
        return interviewRepository.findByUserUserIdAndDelFlagOrderByRegDate(userId, CommonCode.DELETE_N.getCode());
    }

    public InterviewQaListRes selectQaList(Long userId, Long interviewId) {
        userRepository.findById(userId).orElseThrow(() -> new CommonException(ExceptionCode.USER_NULL.getMessage(), ExceptionCode.USER_NULL.getCode()));
        findByUserUserIdAndInterviewIdAndDelFlag(userId, interviewId).orElseThrow(() -> new CommonException(ExceptionCode.INTERVIEW_NULL.getMessage(), ExceptionCode.INTERVIEW_NULL.getCode()));

        QaCountRes qaCountRes = qaService.selectQaListUseCount(interviewId);
        Integer totalCnt = qaCountRes.getTotalCnt();
        Integer useCnt = qaCountRes.getUseCnt();
        Long lastQaId = qaCountRes.getLastQaId();
        List<QaListRes> qaListResList = qaService.selectQaList(interviewId);

        return InterviewQaListRes.builder()
                .interviewId(interviewId)
                .userId(userId)
                .totalCnt(totalCnt)
                .useCnt(useCnt)
                .lastQaId(lastQaId)
                .qaData(qaListResList)
                .build();
    }

    @Transactional
    public CommonResponse initQa(InterviewInitQaReq interviewInitQaReq) {
        Long userId = interviewInitQaReq.getUserId();
        Long interviewId = interviewInitQaReq.getInterviewId();
        userRepository.findById(userId).orElseThrow(() -> new CommonException(ExceptionCode.USER_NULL.getMessage(), ExceptionCode.USER_NULL.getCode()));
        Interview interview = findByUserUserIdAndInterviewIdAndDelFlag(userId, interviewId).orElseThrow(() -> new CommonException(ExceptionCode.INTERVIEW_NULL.getMessage(), ExceptionCode.INTERVIEW_NULL.getCode()));

        Integer status = interview.getStatus();

        // 완료 된 질문은 사용 불가
        if (status == 1) {
            throw new CommonException(ExceptionCode.INTERVIEW_COMPLETE.getMessage(), ExceptionCode.INTERVIEW_COMPLETE.getCode());
        }

        qaService.initQa(interviewId);
        return new CommonResponse<>(MessageCode.OK.getCode(), null, MessageCode.OK.getMessage());
    }

    public void downloadInterview(Long userId, Long interviewId, HttpServletResponse response) throws IOException {
        findByUserUserIdAndInterviewIdAndDelFlag(userId, interviewId).orElseThrow(() -> new CommonException(ExceptionCode.INTERVIEW_NULL.getMessage(), ExceptionCode.INTERVIEW_NULL.getCode()));

        InterviewRes interviewRes = selectInterview(userId, interviewId);

        String title = interviewRes.getTitle();

        String name = URLEncoder.encode(title, "UTF-8");

        String filename = name + ".txt";

        StringBuilder stringBuilder = new StringBuilder();

        String feedback = interviewRes.getInterviewerFeel();

        stringBuilder.append(title + "면접 결과");

        stringBuilder.append("\n");
        stringBuilder.append("\n");

        stringBuilder.append("면접 관의 속마음");
        stringBuilder.append("\n");
        stringBuilder.append(feedback == null ? "" : feedback);

        stringBuilder.append("\n");
        stringBuilder.append("\n");

        stringBuilder.append("티키타카 피드백");

        stringBuilder.append("\n");

        interviewRes.getQaData().stream().forEach(qaListForFeedbackRes -> {
            List<String> keywordList = qaListForFeedbackRes.getKeyword();
            stringBuilder.append("\n");

            if (keywordList != null && keywordList.size() > 0) {
                stringBuilder.append(keywordList.size() > 0 ? keywordList.toString() : "");
                stringBuilder.append("\n");
            }

            String question = qaListForFeedbackRes.getQuestion();
            if (StringUtils.isNotBlank(question)) {
                stringBuilder.append(question);
                stringBuilder.append("\n");
            }

            String answer = qaListForFeedbackRes.getAnswer();
            if (StringUtils.isNotBlank(answer)) {
                stringBuilder.append(answer);
                stringBuilder.append("\n");
            }

            String bestAnswer = qaListForFeedbackRes.getBestAnswer();
            if (StringUtils.isNotBlank(bestAnswer)) {
                stringBuilder.append(bestAnswer);
                stringBuilder.append("\n");
            }
        });

        String content = stringBuilder.toString();

        byte[] fileByte = content.getBytes();

        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(fileByte.length);
        response.setHeader("Content-Disposition", "attachment; FileName=\"" + filename +"\";");
        response.setHeader("Access-Control-Expose-Headers", "X-Filename");
        response.setHeader("X-Filename", filename);
        response.setHeader("Content-Transfer-Encoding",  "binary");
        response.getOutputStream().write(fileByte);

        response.getOutputStream().flush();
        response.getOutputStream().close();
    }

    public CommonResponse generateFeedback(FeedbackGenerateReq feedbackGenerateReq) throws IOException {
        Interview interview = findByUserUserIdAndInterviewIdAndDelFlag(feedbackGenerateReq.getUserId(), feedbackGenerateReq.getInterviewId()).orElseThrow(() -> new CommonException(ExceptionCode.INTERVIEW_NULL.getMessage(), ExceptionCode.INTERVIEW_NULL.getCode()));
        List<Qa> qaList = qaService.findByInterviewInterviewIdAndDelFlagOrderByQaId(feedbackGenerateReq.getInterviewId());
        return feedbackService.generateFeedback(feedbackGenerateReq, interview, qaList);
    }

    @Transactional
    public CommonResponse insertAnswer(QaAnswerInsertReq qaAnswerInsertReq) throws IOException {
        Interview interview = findByUserUserIdAndInterviewIdAndDelFlag(qaAnswerInsertReq.getUserId(), qaAnswerInsertReq.getInterviewId()).orElseThrow(() -> new CommonException(ExceptionCode.INTERVIEW_NULL.getMessage(), ExceptionCode.INTERVIEW_NULL.getCode()));
       return qaService.insertAnswer(qaAnswerInsertReq, interview);
    }

    @Transactional
    public CommonResponse deleteInterview(InterviewDeleteReq interviewDeleteReq) {
        Long userId = interviewDeleteReq.getUserId();
        Long interviewId = interviewDeleteReq.getInterviewId();
        userRepository.findById(userId).orElseThrow(() -> new CommonException(ExceptionCode.USER_NULL.getMessage(), ExceptionCode.USER_NULL.getCode()));
        Interview interview = findByUserUserIdAndInterviewIdAndDelFlag(userId, interviewId).orElseThrow(() -> new CommonException(ExceptionCode.INTERVIEW_NULL.getMessage(), ExceptionCode.INTERVIEW_NULL.getCode()));

        // 피드백 삭제
/*        qaService.selectQaList(interviewId).stream().forEach(qaListRes -> {
            feedbackService.deleteFeedback(qaListRes.getQaId());
        });*/

        // 질문 삭제
        //qaService.deleteQa(interviewId);

        // 면접 삭제
        //interviewRepository.delete(interview);
        
        // 면접 삭제 상태 값 변경으로 수정
        InterviewEditor.InterviewEditorBuilder editorBuilder = interview.toEditor();
        InterviewEditor interviewEditor = editorBuilder.delFlag(CommonCode.DELETE_Y.getCode()).delDate(new Date()).build();
        interview.edit(interviewEditor);

        return new CommonResponse<>(MessageCode.OK.getCode(), null, MessageCode.OK.getMessage());
    }

    public CommonResponse generateQa(QaGenerateReq qaGenerateReq) throws IOException {
        Interview interview = findByUserUserIdAndInterviewIdAndDelFlag(qaGenerateReq.getUserId(), qaGenerateReq.getInterviewId()).orElseThrow(() -> new CommonException(ExceptionCode.INTERVIEW_NULL.getMessage(), ExceptionCode.INTERVIEW_NULL.getCode()));
        qaService.generateQa(qaGenerateReq, interview);
        return new CommonResponse<>(MessageCode.OK.getCode(), null, MessageCode.OK.getMessage());
    }
}
