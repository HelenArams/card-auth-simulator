package com.helenaramyan.cardauth.service;

import com.helenaramyan.cardauth.persistence.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardService {

    private final CardRepository cards;

    public CardService(CardRepository cards) {
        this.cards = cards;
    }

    @Transactional(readOnly = true)
    public CardSummary find(String token) {
        return cards.findById(token)
                .map(CardSummary::of)
                .orElseThrow(() -> new CardNotFoundException(token));
    }
}
