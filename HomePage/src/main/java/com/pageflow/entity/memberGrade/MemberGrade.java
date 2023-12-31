package com.pageflow.entity.memberGrade;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberGrade {
  private Long gradeCode;
  private String gradeName;
  private Double pointRate;
}
