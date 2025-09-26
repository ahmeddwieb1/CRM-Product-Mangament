package org.elmorshedy.meeting.repo;

import org.elmorshedy.meeting.model.MeetingDTO;

import java.util.List;

public interface MeetingRepositoryCustom {
    List<MeetingDTO> findAllWithUserAndLead();

    List<MeetingDTO> findAllWithUserAndLeadWithPage(int page, int size);
}
