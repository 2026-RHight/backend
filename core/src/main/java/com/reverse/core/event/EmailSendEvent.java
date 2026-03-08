package com.reverse.core.event;

public record EmailSendEvent(String to, String subject, String body) {}
