package de.uniks.stpmon.team_m.dto;

import java.util.List;

public record UpdateGroupDto(
        String name,
        List<String> members
) {
}
