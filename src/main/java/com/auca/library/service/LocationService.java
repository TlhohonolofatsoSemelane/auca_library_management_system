package com.auca.library.service;

import com.auca.library.dao.LocationDao;
import com.auca.library.domain.Location;
import java.util.UUID;

public class LocationService {
    private final LocationDao locationDao = new LocationDao();

    public Location createLocation(Location location, UUID parentId) {
        // Rule: Check for duplicate location code
        if (locationDao.findByCode(location.getLocationCode()) != null) {
            throw new IllegalArgumentException("Duplicate location code: " + location.getLocationCode());
        }

        if (location.getType() != Location.LocationType.PROVINCE) {
            if (parentId == null) {
                throw new IllegalArgumentException("Parent ID is required for non-province locations.");
            }
            Location parent = locationDao.findById(parentId);
            if (parent == null) {
                throw new IllegalArgumentException("Parent location not found.");
            }
            location.setParent(parent);
        } else {
            if (parentId != null) {
                throw new IllegalArgumentException("Province cannot have a parent location.");
            }
        }

        locationDao.save(location);
        return location;
    }

    public String getProvinceNameByVillageId(UUID villageId) {
        Location location = locationDao.findById(villageId);
        if (location == null) {
            throw new IllegalArgumentException("Village not found.");
        }

        // Traverse up the tree until we reach the Province level
        Location current = location;
        while (current.getParent() != null) {
            current = current.getParent();
        }

        if (current.getType() != Location.LocationType.PROVINCE) {
            throw new IllegalStateException("Orphaned location branch: Top-most parent is not a Province.");
        }

        return current.getName();
    }
}
