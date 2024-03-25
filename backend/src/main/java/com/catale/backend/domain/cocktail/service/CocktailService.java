package com.catale.backend.domain.cocktail.service;


import com.catale.backend.domain.cocktail.dto.*;
import com.catale.backend.domain.cocktail.entity.Cocktail;
import com.catale.backend.domain.cocktail.repository.CocktailRepository;
import com.catale.backend.domain.like.dto.LikeResponseDto;
import com.catale.backend.domain.like.entity.Like;
import com.catale.backend.domain.like.repository.LikeRepository;
import com.catale.backend.domain.member.entity.Member;
import com.catale.backend.domain.member.service.MemberService;
import com.catale.backend.domain.review.repository.ReviewRepository;
import com.catale.backend.global.exception.cocktail.CocktailNotFoundException;
import com.catale.backend.global.exception.member.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.catale.backend.domain.member.repository.MemberRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Log4j2
@Service
@RequiredArgsConstructor
public class CocktailService {

    private final CocktailRepository cocktailRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final LikeRepository likeRepository;
    private final ReviewRepository reviewRepository;

    //칵테일 전체 리스트 조회
    @Transactional
    public List<CocktailListResponseDto> getAllCocktails(Long memberId) {
        //좋아요 수 많은 순서대로 리스트 가져오기
        List<CocktailListResponseDto> list = cocktailRepository.getCocktails().orElse(new ArrayList<>());
        //칵테일 마다 유저가 좋아요 했는지 유무 저장
        for (CocktailListResponseDto c : list) {
            Optional<LikeResponseDto> likeDto = likeRepository.getIsLike(memberId, c.getId());
            if (!likeDto.isEmpty()) {
                c.setLike(true);
            }
        }
        return list;
    }

    //내가 좋아요 한 칵테일 리스트
    @Transactional
    public List<CocktailGetLikeResponseDto> getLikeCocktails(Long memberId) {
        List<CocktailGetLikeResponseDto> list = cocktailRepository.getLikeCoctails(memberId).orElse(new ArrayList<>());
        return list;
    }

    //칵테일 상세정보 조회
    @Transactional
    public CocktailGetResponseDto getCocktailDetail(Long memberId, Long cocktailId) {
        Cocktail cocktail = cocktailRepository.findById(cocktailId).orElseThrow(NullPointerException::new);
        CocktailGetResponseDto cocktailDto = new CocktailGetResponseDto(cocktail);
        //해당 칵테일의 리뷰 조회 및 dto 저장
        cocktailDto.setReviewList(reviewRepository.findByCocktailId(cocktailId).orElseThrow(NullPointerException::new));
        //해당 칵테일의 좋아요 여부 dto 등록
        Optional<LikeResponseDto> likeDto = likeRepository.getIsLike(memberId, cocktailId);
        if (!likeDto.isEmpty()) {
            cocktailDto.setLike(true);
        }
        return cocktailDto;

    }

    @Transactional
    public CocktailLikeResponseDto getCocktailLikeResult(Long memberId, Long cocktailId) {
//        Member member = memberService.findMember(auth.getName());
//        Long memberId = member.getId();
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        Cocktail cocktail = cocktailRepository.findById(cocktailId).orElseThrow(CocktailNotFoundException::new);
        Like isLike = likeRepository.getLike(memberId, cocktailId).orElse(null);
        CocktailLikeResponseDto responseDto = new CocktailLikeResponseDto();
        responseDto.setCocktailId(cocktailId);

        if (isLike == null) {
            Like like = Like.builder()
                    .cocktail(cocktail)
                    .member(member)
                    .build();
            likeRepository.save(like);
            responseDto.setLiked(true);
        } else {
            likeRepository.delete(isLike);
            responseDto.setLiked(false);
        }
        return responseDto;
    }


    @Transactional
    public void getTodayCocktail(GetTodayCocktailRequest request, Long memberId) {
        //먼저 오늘의 기분과 연관된 색의 칵테일을 하나 선정
        List<Cocktail> cocktailList = cocktailRepository.findAll();
        Cocktail matchedList = findBestMatchingItems(cocktailList, request.getEmotion1(), request.getEmotion2(), request.getEmotion3());

        // FastAPI 호출

    }


    /* 감정 1, 2, 3과의 차이가 적은 칵테일 목록을 뽑는 메서드 */
    private Cocktail findBestMatchingItems(List<Cocktail> cocktailList, int emotion1, int emotion2, int emotion3) {
        List<Cocktail> bestMatches = new ArrayList<>();
        int minDifference = Integer.MAX_VALUE;

        for (Cocktail cocktail : cocktailList) {
            int cocktailAttr1 = cocktail.getEmotion1();
            int cocktailAttr2 = cocktail.getEmotion2();
            int cocktailAttr3 = cocktail.getEmotion3();

            int diff = Math.abs(cocktailAttr1 - emotion1) + Math.abs(cocktailAttr2 - emotion2) + Math.abs(cocktailAttr3 - emotion3);
            ;

            if (diff < minDifference) {
                bestMatches.clear();
                bestMatches.add(cocktail);
                minDifference = diff;
            }
            Collections.shuffle(bestMatches);
            return bestMatches.get(0);
        }
    }

}
