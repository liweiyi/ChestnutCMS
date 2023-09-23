package com.chestnut.vote.listener;

import org.springframework.context.ApplicationEvent;

import com.chestnut.vote.domain.Vote;

import lombok.Getter;

@Getter
public class BeforeVoteAddEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;

	private Vote vote;
	
	public BeforeVoteAddEvent(Object source, Vote vote) {
		super(source);
		this.vote = vote;
	}
}
