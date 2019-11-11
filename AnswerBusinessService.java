package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AnswerBusinessService {

    @Autowired
    UserDao userDao;

    @Autowired
    QuestionDao questionDao;

    @Autowired
    AnswerDao answerDao;

    public String createAnswer(final String answerContent, String accessToken, final String questionId) throws AuthorizationFailedException, InvalidQuestionException {

        UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(accessToken);
        if(userAuthToken == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuthToken.getLogoutAt()!= null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post an answer");
        }
        QuestionEntity questionEntity = questionDao.getQuestionByUuid(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setAns(answerContent);
        answerEntity.setDate(ZonedDateTime.now());
        answerEntity.setQuestion(questionEntity);
        answerEntity.setUser(userAuthToken.getUser());
        answerEntity.setUuid(UUID.randomUUID().toString());
        AnswerEntity createdAnswerEntity = answerDao.crateAnswer(answerEntity);
        return createdAnswerEntity.getUuid();
    }

    public String editAnswerContent(final String answerContent, String accessToken, final String answerId) throws AuthorizationFailedException, AnswerNotFoundException {

        UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(accessToken);
        if(userAuthToken == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuthToken.getLogoutAt()!= null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post an answer");
        }
        AnswerEntity answerEntity = answerDao.getAnswerByUuid(answerId);
        if (answerEntity == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }
        if(!userAuthToken.getUser().getId().equals(answerEntity.getUser().getId())){
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
        }
        answerEntity.setAns(answerContent);
        answerDao.updateAnswer(answerEntity);
        return answerEntity.getUuid();
    }

    public String deleteAnswer(String accessToken, String answerId) throws AuthorizationFailedException, AnswerNotFoundException {

        UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(accessToken);
        if(userAuthToken == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuthToken.getLogoutAt()!= null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post an answer");
        }
        AnswerEntity answerEntity = answerDao.getAnswerByUuid(answerId);
        if (answerEntity == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }
        if(userAuthToken.getUser().getId().equals(answerEntity.getUser().getId()) || "admin".equals(userAuthToken.getUser().getRole())){
            return answerDao.deleteAnswerByUuid(answerId);
        }
        throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
    }

    public List<AnswerEntity> getAllAnswersToQuestion(String accessToken, String questionId) throws AuthorizationFailedException, InvalidQuestionException {

        UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(accessToken);
        if(userAuthToken == null){
            throw new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        if(userAuthToken.getLogoutAt()!= null){
            throw new AuthorizationFailedException("ATHR-002","User is signed out.Sign in first to post an answer");
        }
        QuestionEntity questionEntity = questionDao.getQuestionByUuid(questionId);
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "The question with entered uuid whose details are to be seen does not exist");
        }
        return questionEntity.getAnswerEntities();
    }
}
