package com.catale.backend.domain.diary.entity;

import com.catale.backend.domain.cocktail.entity.Cocktail;
import com.catale.backend.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Where(clause = "is_deleted = false")
@SQLDelete(sql = "UPDATE image SET is_deleted = TRUE WHERE image_id = ?")
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cocktail_id")
    private Cocktail cocktail;

    @Column(name = "mood", nullable = false)
    private int mood;

    @Column(name = "comment", nullable = false)
    private String comment;

    @Column(name = "reason",nullable = false)
    private String reason;

    @Column(name = "diary_month", nullable = false)
    private int month;

    @Column(name = "emotion1", nullable = false)
    private int emotion1;

    @Column(name = "emotion2")
    private int emotion2;

    @Column(name = "emotion3")
    private int emotion3;


}
