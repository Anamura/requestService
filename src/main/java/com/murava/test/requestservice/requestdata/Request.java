package com.murava.test.requestservice.requestdata;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Request {
    private final Instant created;
    private final Integer clientId;
    private final UUID requestId;

    public Request(Instant created, Integer clientId) {
        this.created = created;
        this.clientId = clientId;
        this.requestId = UUID.randomUUID();
    }

    public Request(Clock clock, Integer clientId) {
        this.created = clock.instant();
        this.clientId = clientId;
        this.requestId = UUID.randomUUID();
    }

    public Request(Integer clientId) {
        this.created = Instant.now();
        this.clientId = clientId;
        this.requestId = UUID.randomUUID();
    }

    public Request(Integer clientId, UUID requestId) {
        this.created = Instant.now();
        this.clientId = clientId;
        this.requestId = UUID.randomUUID();
    }

    public Request(Instant created, Integer clientId, UUID requestId) {
        this.created = created;
        this.clientId = clientId;
        this.requestId = requestId;
    }

    /**
     * Creates a new Request with the same client id and service id but a new creation instant.
     * @param other the service that will be copied
     */
    private Request(Request other) {
        this.created = Instant.now();
        this.clientId = other.clientId;
        this.requestId = other.requestId;
    }

    public Instant getCreated() {
        return created;
    }

    public Integer getClientId() {
        return clientId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    /**
     * Returns a new service with the same {@link #getClientId() client id} and the same
     * {@link #getRequestId() service id} but a new {@link #getCreated() creation instant}.
     */
    public Request duplicate() {
        return new Request(this);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.created);
        hash = 29 * hash + this.clientId;
        return hash;
    }
    @SuppressFBWarnings(value = "RC_REF_COMPARISON")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Request other = (Request) obj;
        if (this.clientId != other.clientId) {
            return false;
        }
        if (!Objects.equals(this.created, other.created)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder(50);
        Instant created = getCreated();
        Integer clientId = getClientId();
        UUID requestId = getRequestId();

        text.append(getClass().getSimpleName());
        if (created != null) {
            text.append(": ");
            text.append(created);
        }
        if (clientId != null) {
            text.append(" clientId: ");
            text.append(clientId);
        }
        if (requestId != null) {
            text.append(" requestId: ");
            text.append(requestId);
        }
        return text.toString();
    }
}