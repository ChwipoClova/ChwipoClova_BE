package com.chwipoClova.subscription.service;

import com.chwipoClova.article.entity.Feed;
import com.chwipoClova.article.repository.FeedRepository;
import com.chwipoClova.common.exception.CommonException;
import com.chwipoClova.common.exception.ExceptionCode;
import com.chwipoClova.common.response.CommonResponse;
import com.chwipoClova.common.response.MessageCode;
import com.chwipoClova.common.utils.DateUtils;
import com.chwipoClova.subscription.entity.Subscription;
import com.chwipoClova.subscription.repository.SubscriptionRepository;
import com.chwipoClova.subscription.request.SubscriptionReq;
import com.chwipoClova.subscription.response.SubscriptionRes;
import com.chwipoClova.user.entity.User;
import com.chwipoClova.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    private final UserRepository userRepository;

    private final FeedRepository feedRepository;

    private final EmailService emailService;

    @Transactional
    public CommonResponse subscription(SubscriptionReq subscriptionReq) {
        Long userId = subscriptionReq.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new CommonException(ExceptionCode.USER_NULL.getMessage(), ExceptionCode.USER_NULL.getCode()));

        Optional<Subscription> userSubscription = subscriptionRepository.findByUser_UserId(userId);
        if (userSubscription.isPresent()) {
            // 이미 구독 중인 경우
            // 삭제 여부 변경
            userSubscription.get().setDelFlag(0);
        } else {
            // 신규 저장
            Subscription subscription = new Subscription();
            subscription.setName(user.getName());
            subscription.setEmail(user.getEmail());
            subscription.setThumbnail(user.getThumbnailImage());
            subscription.setDivision("K");
            subscription.setPublished(Instant.now());
            subscription.setDelFlag(0);
            subscription.setUser(user);
            subscriptionRepository.save(subscription);
        }
        return new CommonResponse<>(MessageCode.OK.getCode(), null, MessageCode.OK.getMessage());
    }

    @Transactional
    public CommonResponse subscriptionCancel(SubscriptionReq subscriptionReq) {
        Long userId = subscriptionReq.getUserId();
        Subscription subscription = subscriptionRepository.findByUser_UserId(userId).orElseThrow(() -> new CommonException(ExceptionCode.SUBSCRIPTION_NULL.getMessage(), ExceptionCode.SUBSCRIPTION_NULL.getCode()));
        subscription.setDelFlag(1); // 논리 삭제로 변경
        // subscriptionRepository.deleteById(subscription.getId());
        return new CommonResponse<>(MessageCode.OK.getCode(), null, MessageCode.OK.getMessage());
    }

    public CommonResponse sendSubscriptionEmail() throws IOException {
        List<Subscription> subscriptionList = subscriptionRepository.findAll();

        LocalDate localDate = LocalDate.now();
        // 지난주 목요일
        Instant lastThurDay = DateUtils.getLocalDateToInstant(DateUtils.calculateLastWeek(localDate, DayOfWeek.THURSDAY));

        // 이번주 수요일
        Instant thisWednesDay = DateUtils.getLocalDateToInstant(DateUtils.calculateThisWeek(localDate, DayOfWeek.WEDNESDAY));

        // 지난주 목요일 이번주 수요일
        List<Feed> feedList = feedRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(lastThurDay, thisWednesDay);

        emailService.sendEmail(subscriptionList, feedList);
        return new CommonResponse<>(MessageCode.OK.getCode(), null, MessageCode.OK.getMessage());
    }

    public CommonResponse subscriptionCheck(Long userId) {
        SubscriptionRes subscriptionRes = new SubscriptionRes();
        Optional<Subscription> subscription = subscriptionRepository.findByUser_UserId(userId);

        subscriptionRes.setCheck(subscription.isPresent());
        return new CommonResponse<>(MessageCode.OK.getCode(), subscriptionRes, MessageCode.OK.getMessage());
    }
}
