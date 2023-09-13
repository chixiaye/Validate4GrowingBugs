package model;

import com.sun.istack.internal.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chixiaye
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BugProjectDO {
    String no;
    String projectId;
    String projectName;
    @Nullable
    String locator;
    String numbers;
    String bugIds;

}
