package com.turisup.resources.model.admin;

import com.turisup.resources.model.Organization;
import com.turisup.resources.model.Region;
import lombok.Data;

@Data
public class AdminUserResource {
    Organization organization;
    Region region;

    public AdminUserResource(Organization myOrg, Region myRegion) {
        this.organization=myOrg;
        this.region=myRegion;
    }
}
