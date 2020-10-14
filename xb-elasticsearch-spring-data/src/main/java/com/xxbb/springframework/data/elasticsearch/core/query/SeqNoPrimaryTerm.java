package com.xxbb.springframework.data.elasticsearch.core.query;

import java.util.Objects;

public final class SeqNoPrimaryTerm {
    private final long sequenceNumber;
    private final long primaryTerm;

    public SeqNoPrimaryTerm(long sequenceNumber, long primaryTerm) {
        if (sequenceNumber < 0) {
            throw new IllegalArgumentException("seq_no should not be negative, but it is " + sequenceNumber);
        }

        if (primaryTerm <= 0) {
            throw new IllegalArgumentException("primary_term should be positive, but it is" + primaryTerm);
        }
        this.sequenceNumber = sequenceNumber;
        this.primaryTerm = primaryTerm;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public long getPrimaryTerm() {
        return primaryTerm;
    }

    @Override
    public String toString() {
        return "SeqNoPrimaryTerm{" +
                "sequenceNumber=" + sequenceNumber +
                ", primaryTerm=" + primaryTerm +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeqNoPrimaryTerm)) return false;
        SeqNoPrimaryTerm that = (SeqNoPrimaryTerm) o;
        return sequenceNumber == that.sequenceNumber &&
                primaryTerm == that.primaryTerm;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceNumber, primaryTerm);
    }
}
