package auth.service.app.util;

import static org.mockito.Mockito.*;

import auth.service.app.model.entity.PlatformEntity;
import auth.service.app.model.entity.ProfileEntity;
import auth.service.app.model.enums.TypeEnums;
import auth.service.app.model.events.ProfileEvent;
import auth.service.app.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventListeners Unit Tests")
class EventListenersTest {

    @Mock private EmailService emailService;
    @Mock private ProfileEvent profileEvent;
    @Mock private PlatformEntity platformEntity;
    @Mock private ProfileEntity profileEntity;

    @InjectMocks private EventListeners eventListeners;

    private static final Long PLATFORM_ID = 1L;
    private static final Long PROFILE_ID = 100L;
    private static final String BASE_URL = "https://example.com";

    @BeforeEach
    void setUp() {
        when(profileEvent.getPlatformEntity()).thenReturn(platformEntity);
        when(profileEvent.getProfileEntity()).thenReturn(profileEntity);
        when(profileEvent.getBaseUrl()).thenReturn(BASE_URL);
        when(platformEntity.getId()).thenReturn(PLATFORM_ID);
        when(profileEntity.getId()).thenReturn(PROFILE_ID);
    }

    @Nested
    @DisplayName("handleProfileEventForValidation() - CREATE event tests")
    class CreateEventTests {

        @Test
        @DisplayName("Should send validation email when event type is CREATE")
        void shouldSendValidationEmailForCreateEvent() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.CREATE);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService, times(1))
                    .sendProfileValidationEmail(platformEntity, profileEntity, BASE_URL);
            verify(emailService, never()).sendProfilePasswordEmail(any(), any());
        }

        @Test
        @DisplayName("Should retrieve all required fields from profile event for CREATE")
        void shouldRetrieveAllFieldsForCreateEvent() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.CREATE);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(profileEvent).getEventType();
            verify(profileEvent).getPlatformEntity();
            verify(profileEvent).getProfileEntity();
            verify(profileEvent).getBaseUrl();
        }

        @Test
        @DisplayName("Should call sendProfileValidationEmail with correct parameters for CREATE")
        void shouldCallSendValidationEmailWithCorrectParameters() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.CREATE);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService).sendProfileValidationEmail(
                    eq(platformEntity), eq(profileEntity), eq(BASE_URL));
        }
    }

    @Nested
    @DisplayName("handleProfileEventForValidation() - UPDATE_EMAIL event tests")
    class UpdateEmailEventTests {

        @Test
        @DisplayName("Should send validation email when event type is UPDATE_EMAIL")
        void shouldSendValidationEmailForUpdateEmailEvent() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.UPDATE_EMAIL);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService, times(1))
                    .sendProfileValidationEmail(platformEntity, profileEntity, BASE_URL);
            verify(emailService, never()).sendProfilePasswordEmail(any(), any());
        }

        @Test
        @DisplayName("Should retrieve all required fields from profile event for UPDATE_EMAIL")
        void shouldRetrieveAllFieldsForUpdateEmailEvent() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.UPDATE_EMAIL);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(profileEvent).getEventType();
            verify(profileEvent).getPlatformEntity();
            verify(profileEvent).getProfileEntity();
            verify(profileEvent).getBaseUrl();
        }

        @Test
        @DisplayName("Should call sendProfileValidationEmail with correct parameters for UPDATE_EMAIL")
        void shouldCallSendValidationEmailWithCorrectParameters() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.UPDATE_EMAIL);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService).sendProfileValidationEmail(
                    eq(platformEntity), eq(profileEntity), eq(BASE_URL));
        }
    }

    @Nested
    @DisplayName("handleProfileEventForValidation() - UPDATE_PASSWORD event tests")
    class UpdatePasswordEventTests {

        @Test
        @DisplayName("Should send password email when event type is UPDATE_PASSWORD")
        void shouldSendPasswordEmailForUpdatePasswordEvent() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.UPDATE_PASSWORD);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService, times(1)).sendProfilePasswordEmail(platformEntity, profileEntity);
            verify(emailService, never()).sendProfileValidationEmail(any(), any(), any());
        }

        @Test
        @DisplayName("Should retrieve required fields from profile event for UPDATE_PASSWORD")
        void shouldRetrieveRequiredFieldsForUpdatePasswordEvent() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.UPDATE_PASSWORD);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(profileEvent).getEventType();
            verify(profileEvent).getPlatformEntity();
            verify(profileEvent).getProfileEntity();
            // baseUrl is not needed for password emails
        }

        @Test
        @DisplayName("Should call sendProfilePasswordEmail with correct parameters")
        void shouldCallSendPasswordEmailWithCorrectParameters() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.UPDATE_PASSWORD);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService).sendProfilePasswordEmail(eq(platformEntity), eq(profileEntity));
        }

        @Test
        @DisplayName("Should not use baseUrl for UPDATE_PASSWORD event")
        void shouldNotUseBaseUrlForPasswordEvent() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.UPDATE_PASSWORD);

            eventListeners.handleProfileEventForValidation(profileEvent);

            // Verify that baseUrl is retrieved from the event (for logging)
            verify(profileEvent).getBaseUrl();
            // But verify it's not passed to sendProfilePasswordEmail
            verify(emailService).sendProfilePasswordEmail(platformEntity, profileEntity);
            verify(emailService, never()).sendProfileValidationEmail(any(), any(), anyString());
        }
    }

    @Nested
    @DisplayName("handleProfileEventForValidation() - Edge cases and null handling")
    class EdgeCasesAndNullHandlingTests {

        @Test
        @DisplayName("Should handle null baseUrl for CREATE event")
        void shouldHandleNullBaseUrlForCreateEvent() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.CREATE);
            when(profileEvent.getBaseUrl()).thenReturn(null);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService).sendProfileValidationEmail(platformEntity, profileEntity, null);
        }

        @Test
        @DisplayName("Should handle empty baseUrl for UPDATE_EMAIL event")
        void shouldHandleEmptyBaseUrlForUpdateEmailEvent() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.UPDATE_EMAIL);
            when(profileEvent.getBaseUrl()).thenReturn("");

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService).sendProfileValidationEmail(platformEntity, profileEntity, "");
        }

        @Test
        @DisplayName("Should handle baseUrl with special characters")
        void shouldHandleBaseUrlWithSpecialCharacters() {
            String specialUrl = "https://example.com:8443/app?param=value&test=123";
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.CREATE);
            when(profileEvent.getBaseUrl()).thenReturn(specialUrl);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService).sendProfileValidationEmail(platformEntity, profileEntity, specialUrl);
        }

        @Test
        @DisplayName("Should handle platform entity with null ID")
        void shouldHandlePlatformEntityWithNullId() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.CREATE);
            when(platformEntity.getId()).thenReturn(null);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService).sendProfileValidationEmail(platformEntity, profileEntity, BASE_URL);
        }

        @Test
        @DisplayName("Should handle profile entity with null ID")
        void shouldHandleProfileEntityWithNullId() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.UPDATE_PASSWORD);
            when(profileEntity.getId()).thenReturn(null);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService).sendProfilePasswordEmail(platformEntity, profileEntity);
        }
    }

    @Nested
    @DisplayName("Event handling flow tests")
    class EventHandlingFlowTests {

        @Test
        @DisplayName("Should handle multiple consecutive CREATE events")
        void shouldHandleMultipleCreateEvents() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.CREATE);

            eventListeners.handleProfileEventForValidation(profileEvent);
            eventListeners.handleProfileEventForValidation(profileEvent);
            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService, times(3))
                    .sendProfileValidationEmail(platformEntity, profileEntity, BASE_URL);
        }

        @Test
        @DisplayName("Should handle different event types in sequence")
        void shouldHandleDifferentEventTypesInSequence() {
            // First event: CREATE
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.CREATE);
            eventListeners.handleProfileEventForValidation(profileEvent);

            // Second event: UPDATE_PASSWORD
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.UPDATE_PASSWORD);
            eventListeners.handleProfileEventForValidation(profileEvent);

            // Third event: UPDATE_EMAIL
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.UPDATE_EMAIL);
            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(emailService, times(2))
                    .sendProfileValidationEmail(platformEntity, profileEntity, BASE_URL);
            verify(emailService, times(1)).sendProfilePasswordEmail(platformEntity, profileEntity);
        }

        @Test
        @DisplayName("Should maintain independence between event handling calls")
        void shouldMaintainIndependenceBetweenCalls() {
            // First call with CREATE
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.CREATE);
            eventListeners.handleProfileEventForValidation(profileEvent);

            // Only one email should be sent (from CREATE)
            verify(emailService, times(1))
                    .sendProfileValidationEmail(platformEntity, profileEntity, BASE_URL);
            verify(emailService, never()).sendProfilePasswordEmail(any(), any());
        }
    }

    @Nested
    @DisplayName("Logging behavior tests")
    class LoggingBehaviorTests {

        @Test
        @DisplayName("Should retrieve platform and profile IDs for logging purposes")
        void shouldRetrieveIdsForLogging() {
            when(profileEvent.getEventType()).thenReturn(TypeEnums.EventType.CREATE);

            eventListeners.handleProfileEventForValidation(profileEvent);

            verify(platformEntity, atLeastOnce()).getId();
            verify(profileEntity, atLeastOnce()).getId();
        }
    }
}

