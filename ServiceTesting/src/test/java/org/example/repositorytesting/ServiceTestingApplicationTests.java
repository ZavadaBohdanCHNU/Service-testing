package org.example.repositorytesting;

/**
 *   @author Bohdan
 *   @project repositorytesting
 *   @class ServiceTestingApplicationTests.java
 *   @version 1.0
 *   @since 4/30/2025 17:00
 */

import org.example.repositorytesting.model.User;
import org.example.repositorytesting.repository.UserRepository;
import org.example.repositorytesting.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServiceTestingApplicationTests {

    @Autowired
    private UserRepository repository;
    
    @Autowired
    private UserService service;

    private List<User> initialUsers;

    @BeforeAll
    void init() {
        initialUsers = new ArrayList<>();
        initialUsers.add(User.builder()
                .name("Freddie Mercury")
                .code("Queen")
                .description("vocal, piano")
                .createDate(LocalDateTime.now())
                .updateDates(new ArrayList<>())
                .build());
        
        initialUsers.add(User.builder()
                .name("Paul McCartney")
                .code("Beatles")
                .description("bass, vocal")
                .createDate(LocalDateTime.now())
                .updateDates(new ArrayList<>())
                .build());
                
        initialUsers.add(User.builder()
                .name("Mick Jagger")
                .code("Rolling Stones")
                .description("vocal")
                .createDate(LocalDateTime.now())
                .updateDates(new ArrayList<>())
                .build());
    }

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        repository.saveAll(initialUsers);
    }

    @Test
    void shouldCreateNewUser() {
        // given
        User johnLennon = User.builder()
                .name("John Lennon")
                .code("Beatles")
                .description("rhythm guitar, vocal")
                .build();

        // when
        User saved = service.create(johnLennon);

        // then
        assertNotNull(saved.getId());
    }

    @Test
    void shouldGetAllUsers() {
        // when
        List<User> users = service.getAll();
        
        // then
        assertEquals(3, users.size());
    }

    @Test
    void shouldUpdateUserDescription() {
        // given
        List<User> users = service.getAll();
        User user = users.get(0);
        user.setDescription("updated description");
        
        // when
        User updated = service.update(user);
        
        // then
        assertEquals("updated description", updated.getDescription());
    }

    @Test
    void shouldDeleteUser() {
        // given
        List<User> beforeDelete = service.getAll();
        User userToDelete = beforeDelete.get(0);
        
        // when
        service.delete(userToDelete.getId());
        List<User> afterDelete = service.getAll();
        
        // then
        assertEquals(beforeDelete.size() - 1, afterDelete.size());
    }

    @Test
    void shouldFindByExactName() {
        // when
        List<User> found = service.findByName("Paul McCartney");
        
        // then
        assertFalse(found.isEmpty());
    }

    @Test
    void shouldFindByBandCode() {
        // when
        List<User> beatlesMembers = repository.findByCode("Beatles");
        
        // then
        assertFalse(beatlesMembers.isEmpty());
    }

    @Test
    void shouldTrackMultipleUpdates() {
        // given
        List<User> users = service.getAll();
        User user = users.get(0);
        
        // when
        user.setDescription("update 1");
        User updated = service.update(user);
        
        // then
        assertFalse(updated.getUpdateDates().isEmpty());
    }

    @Test
    void shouldCreateUserFromRequest() {
        // given
        User newUser = User.builder()
                .name("George Harrison")
                .code("Beatles")
                .description("lead guitar")
                .build();

        // when
        User created = service.create(newUser);

        // then
        assertNotNull(created.getId());
        assertEquals("George Harrison", created.getName());
        assertEquals("Beatles", created.getCode());
    }

    @Test
    void shouldUpdateUserFromRequest() {
        // given
        List<User> users = service.getAll();
        User existingUser = users.get(0);
        String updatedName = "Updated Name";
        String updatedCode = "Updated Code";
        String updatedDescription = "Updated Description";
        
        // when
        existingUser.setName(updatedName);
        existingUser.setCode(updatedCode);
        existingUser.setDescription(updatedDescription);
        User updated = service.update(existingUser);

        // then
        assertNotNull(updated);
        assertEquals(updatedName, updated.getName());
        assertEquals(updatedCode, updated.getCode());
        assertEquals(updatedDescription, updated.getDescription());
        assertFalse(updated.getUpdateDates().isEmpty());
    }

    @Test
    void shouldSetCreationDate() {
        // given
        User user = User.builder()
                .name("New Artist")
                .code("Test")
                .description("test")
                .build();

        // when
        User created = service.create(user);

        // then
        assertNotNull(created.getCreateDate());
    }

    @Test
    void shouldHandleEmptyName() {
        // given
        User user = User.builder()
                .name("")
                .code("Test")
                .description("test")
                .build();

        // when
        User created = service.create(user);

        // then
        assertNotNull(created.getId());
        assertEquals("", created.getName());
    }

    @Test
    void shouldHandleNullCode() {
        // given
        User user = User.builder()
                .name("Test Artist")
                .description("test")
                .build();

        // when
        User created = service.create(user);

        // then
        assertNotNull(created.getId());
        assertNull(created.getCode());
    }

    @Test
    void shouldFindByPartialName() {
        // when
        List<User> found = service.findByName("Freddie Mercury");

        // then
        assertFalse(found.isEmpty());
        assertEquals("Freddie Mercury", found.get(0).getName());
    }

    @Test
    void shouldNotFindNonExistentUser() {
        // when
        List<User> found = service.findByName("Non Existent");

        // then
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldHandleMultipleUsersInBand() {
        // given
        User ringo = User.builder()
                .name("Ringo Starr")
                .code("Beatles")
                .description("drums")
                .build();

        // when
        service.create(ringo);
        List<User> beatles = repository.findByCode("Beatles");

        // then
        assertEquals(2, beatles.size());
    }

    @Test
    void shouldPreserveCreateDateOnUpdate() {
        // given
        List<User> users = service.getAll();
        User user = users.get(0);
        LocalDateTime originalCreateDate = user.getCreateDate();

        // when
        user.setDescription("updated");
        User updated = service.update(user);

        // then
        assertEquals(originalCreateDate, updated.getCreateDate());
    }

    @Test
    void shouldHandleSpecialCharacters() {
        // given
        User user = User.builder()
                .name("AC/DC")
                .code("Rock&Roll")
                .description("!@#$%")
                .build();

        // when
        User created = service.create(user);

        // then
        assertEquals("AC/DC", created.getName());
        assertEquals("Rock&Roll", created.getCode());
    }

    @Test
    void shouldHandleLongDescription() {
        // given
        String longDesc = "a".repeat(1000);
        User user = User.builder()
                .name("Test")
                .code("Test")
                .description(longDesc)
                .build();

        // when
        User created = service.create(user);

        // then
        assertEquals(1000, created.getDescription().length());
    }

    @Test
    void shouldHandleNonExistentUpdate() {
        // given
        User nonExistent = User.builder()
                .id("non-existent-id")
                .name("Test")
                .build();

        // when
        User result = service.update(nonExistent);

        // then
        assertNull(result);
    }

    @Test
    void shouldDeleteNonExistentUser() {
        // when & then
        assertDoesNotThrow(() -> service.delete("non-existent-id"));
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }
}


