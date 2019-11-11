package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.AnswerDetailsResponse;
import com.upgrad.quora.api.model.AnswerEditRequest;
import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.AnswerResponse;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.entity.AnswerEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class AnswerController {

    @Autowired
    private AnswerBusinessService answerBusinessService;

    @RequestMapping(method = RequestMethod.POST, path = "question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> createAnswer(final AnswerRequest answerRequest, @PathVariable("questionId") String questionId, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {

        final AnswerResponse answerResponse = new AnswerResponse();

        String accessToken = authorization.split("Bearer ")[1];
        String uuid = answerBusinessService.createAnswer(answerRequest.getAnswer(), accessToken, questionId);

        answerResponse.id(uuid).status("ANSWER CREATED");

        return new ResponseEntity<>(answerResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "answer/edit/{answerId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> editAnswerContent(final AnswerEditRequest answerEditRequest, @PathVariable("answerId") String answerId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {

        final AnswerResponse answerResponse = new AnswerResponse();

        String accessToken = authorization.split("Bearer ")[1];
        String uuid = answerBusinessService.editAnswerContent(answerEditRequest.getContent(), accessToken, answerId);
        answerResponse.id(uuid).status("ANSWER EDITED");
        return new ResponseEntity<>(answerResponse, HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "answer/delete/{answerId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AnswerResponse> deleteAnswer(@PathVariable("answerId") String answerId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {

        final AnswerResponse answerResponse = new AnswerResponse();

        String accessToken = authorization.split("Bearer ")[1];
        String uuid = answerBusinessService.deleteAnswer(accessToken, answerId);
        answerResponse.id(uuid).status("ANSWER DELETED");
        return new ResponseEntity<>(answerResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "answer/all/{questionId}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersToQuestion(@PathVariable("questionId") String questionId, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {

        List<AnswerDetailsResponse> answerResponses = new ArrayList<>();

        String accessToken = authorization.split("Bearer ")[1];
        List<AnswerEntity> answers = answerBusinessService.getAllAnswersToQuestion(accessToken, questionId);

        answers.forEach(answerEntity -> {
            AnswerDetailsResponse answerDetailsResponse = new AnswerDetailsResponse();
            answerDetailsResponse.setId(answerEntity.getUuid());
            answerDetailsResponse.setAnswerContent(answerEntity.getAns());
            answerDetailsResponse.setQuestionContent(answerEntity.getQuestion().getContent());
            answerResponses.add(answerDetailsResponse);
        });
        return new ResponseEntity<>(answerResponses, HttpStatus.CREATED);
    }


    //
}
