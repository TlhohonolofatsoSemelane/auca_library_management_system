package com.auca.library.service;

import com.auca.library.domain.Location;
import com.auca.library.domain.MembershipType;
import com.auca.library.domain.User;
import com.auca.library.util.HibernateUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import java.util.UUID;
import static org.junit.Assert.*;

public class UserServiceTest {
    private UserService userService;
    private LocationService locationService;

    @Before
    public void setUp() {
        userService = new UserService();
        locationService = new LocationService();
    }


    @Test
    public void validPersonId_returnsCorrectProvinceName() {
        // Using unique codes (KGL2, GSB2, KCY2, KMT2, IYN2) to avoid duplicate key violations
        Location province = locationService.createLocation(new Location("Kigali City", "KGL2", Location.LocationType.PROVINCE), null);
        Location district = locationService.createLocation(new Location("Gasabo", "GSB2", Location.LocationType.DISTRICT), province.getId());
        Location sector = locationService.createLocation(new Location("Kacyiru", "KCY2", Location.LocationType.SECTOR), district.getId());
        Location cell = locationService.createLocation(new Location("Kamatamu", "KMT2", Location.LocationType.CELL), sector.getId());
        Location village = locationService.createLocation(new Location("Inyange", "IYN2", Location.LocationType.VILLAGE), cell.getId());

        User user = new User("John", "Doe", "johndoe_unique", "password123");
        user.setVillage(village);
        userService.registerUser(user);

        String provinceName = userService.getProvinceNameByPersonId(user.getId());
        assertEquals("Kigali City", provinceName);
    }

    @Test
    public void authenticate_correctCredentials_returnsTrue() {
        User user = new User("Alice", "Smith", "alice", "securePass");
        userService.registerUser(user);

        assertTrue(userService.authenticate("alice", "securePass"));
    }

    @Test
    public void authenticate_wrongPassword_returnsFalse() {
        User user = new User("Bob", "Jones", "bob", "myPassword");
        userService.registerUser(user);

        assertFalse(userService.authenticate("bob", "wrongPassword"));
    }

    @Test
    public void authenticate_unknownUsername_returnsFalse() {
        assertFalse(userService.authenticate("ghostUser", "anyPassword"));
    }

    @Test
    public void authenticate_nullOrBlankInput_returnsFalse() {
        assertFalse(userService.authenticate("", ""));
        assertFalse(userService.authenticate(null, null));
    }

    @Test
    public void registerMembership_gold_createsPendingMembershipLinkedToGoldType() {
        User user = new User("Charlie", "Brown", "charlie", "pass");
        userService.registerUser(user);

        User updatedUser = userService.registerMembership(user.getId(), MembershipType.GOLD);
        assertEquals(MembershipType.GOLD, updatedUser.getMembershipType());
        assertFalse(updatedUser.isMembershipApproved());
    }

    @Test(expected = IllegalStateException.class)
    public void registerMembership_userAlreadyHasActiveMembership_throwsException() {
        User user = new User("Dave", "Miller", "dave", "pass");
        userService.registerUser(user);

        userService.registerMembership(user.getId(), MembershipType.SILVER);
        userService.approveMembership(user.getId());

        // Trying to register again while having an active membership should throw exception
        userService.registerMembership(user.getId(), MembershipType.GOLD);
    }

        @Test
    public void goldMember_withFourActiveBorrows_canBorrowAFifth() {
        User user = new User("Gold", "Member", "goldy_unique", "pass");
        userService.registerUser(user);
        
        // Register and approve membership
        userService.registerMembership(user.getId(), MembershipType.GOLD);
        userService.approveMembership(user.getId());

        // Force the local object to match the approved database state perfectly
        user.setMembershipType(MembershipType.GOLD);
        user.setMembershipApproved(true);
        user.setActiveBorrowsCount(4);
        
        userService.updateUser(user);

        // Should pass without throwing exception
        userService.validateBorrowLimit(user.getId());
    }

    @Test(expected = IllegalStateException.class)
    public void goldMember_withFiveActiveBorrows_cannotBorrowASixth() {
        User user = new User("GoldMax", "Member", "goldmax_unique", "pass");
        userService.registerUser(user);
        userService.registerMembership(user.getId(), MembershipType.GOLD);
        userService.approveMembership(user.getId());

        user.setActiveBorrowsCount(5);
        userService.updateUser(user); 

        userService.validateBorrowLimit(user.getId()); 
    }

    @Test(expected = IllegalStateException.class)
    public void silverMember_withThreeActiveBorrows_isBlocked() {
        User user = new User("Silver", "Member", "silver_unique", "pass");
        userService.registerUser(user);
        userService.registerMembership(user.getId(), MembershipType.SILVER);
        userService.approveMembership(user.getId());

        user.setActiveBorrowsCount(3);
        userService.updateUser(user); 

        userService.validateBorrowLimit(user.getId()); 
    }

    @Test(expected = IllegalStateException.class)
    public void striverMember_withTwoActiveBorrows_isBlocked() {
        User user = new User("Striver", "Member", "striver_unique", "pass");
        userService.registerUser(user);
        userService.registerMembership(user.getId(), MembershipType.STRIVER);
        userService.approveMembership(user.getId());

        user.setActiveBorrowsCount(2);
        userService.updateUser(user); 

        userService.validateBorrowLimit(user.getId()); 
    }
}