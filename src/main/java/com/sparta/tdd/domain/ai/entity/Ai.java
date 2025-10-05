package com.sparta.tdd.domain.ai.entity;

import com.sparta.tdd.domain.user.entity.User;
import com.sparta.tdd.global.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "p_ai")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ai extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ai_id", nullable = false)
    private UUID id;

    @Column(name = "input_text", nullable = false, length = 50)
    private String inputText;

    @Column(name = "output_text", columnDefinition = "TEXT")
    private String outputText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Ai(String inputText, String outputText, User user) {
        this.inputText = inputText;
        this.outputText = outputText;
        this.user = user;
    }

    public static Ai of(String inputText, String outputText, User user) {
        return Ai.builder()
                .inputText(inputText)
                .outputText(outputText)
                .user(user)
                .build();
    }
}
