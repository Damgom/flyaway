package com.codestates.flyaway.member.repository;

import com.codestates.flyaway.domain.member.entity.Member;
import com.codestates.flyaway.domain.member.repository.MemberRepository;
import com.codestates.flyaway.domain.record.entity.Record;
import com.codestates.flyaway.domain.record.repository.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static java.time.LocalDate.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RecordRepository recordRepository;

    @BeforeEach
    void before() {
        Member member1 = new Member(1L, new ArrayList<>(), "kim", "member1@gmail.com", "pw", 0);

        Record record1 = new Record(now().toString(), 10);
        record1.setMember(member1);
        Record record2 = new Record(now().toString(), 20);
        record2.setMember(member1);

        memberRepository.save(member1);
        recordRepository.save(record1);
        recordRepository.save(record2);
    }


    @DisplayName("회원의 모든 정보를 가져오기 (findByIdFetch)")
    @Test
    void fetchJoinTest() {
        Member findMember = memberRepository.findByIdFetch(1)
                .orElseThrow(() -> new RuntimeException("error"));

        assertThat(findMember.getRecords()).hasSize(2);
        assertThat(findMember.getEmail()).isEqualTo("member1@gmail.com");
    }
}