package tn.esprit.eventsproject.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServicesImplTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    @Test
    void addParticipant_doitEnregistrerLeParticipant() {
        Participant p = mock(Participant.class);
        when(participantRepository.save(p)).thenReturn(p);

        Participant result = eventServices.addParticipant(p);

        assertSame(p, result);
        verify(participantRepository).save(p);
    }

    @Test
    void addAffectEvenParticipantParId_listeEventsNull_creeLaListe() {
        Event event = mock(Event.class);
        Participant p = mock(Participant.class);
        when(participantRepository.findById(1)).thenReturn(Optional.of(p));
        when(p.getEvents()).thenReturn(null);
        when(eventRepository.save(event)).thenReturn(event);

        Event result = eventServices.addAffectEvenParticipant(event, 1);

        assertSame(event, result);
        verify(p).setEvents(any());
        verify(eventRepository).save(event);
    }

    @Test
    void addAffectEvenParticipantParId_listeEventsExiste_ajouteLEvent() {
        Event event = mock(Event.class);
        Participant p = mock(Participant.class);
        Set<Event> events = new HashSet<>();
        when(participantRepository.findById(2)).thenReturn(Optional.of(p));
        when(p.getEvents()).thenReturn(events);
        when(eventRepository.save(event)).thenReturn(event);

        eventServices.addAffectEvenParticipant(event, 2);

        assertTrue(events.contains(event));
        verify(p, never()).setEvents(any());
    }

    @Test
    void addAffectEvenParticipantParEvent_listeEventsNull_affecteLeParticipant() {
        Event event = mock(Event.class);
        Participant p = mock(Participant.class);
        Set<Participant> participants = new HashSet<>();
        participants.add(p);
        when(event.getParticipants()).thenReturn(participants);
        when(p.getIdPart()).thenReturn(1);
        when(participantRepository.findById(1)).thenReturn(Optional.of(p));
        when(p.getEvents()).thenReturn(null);
        when(eventRepository.save(event)).thenReturn(event);

        Event result = eventServices.addAffectEvenParticipant(event);

        assertSame(event, result);
        verify(p).setEvents(any());
    }

    @Test
    void addAffectEvenParticipantParEvent_listeEventsExiste_ajouteLEvent() {
        Event event = mock(Event.class);
        Participant p = mock(Participant.class);
        Set<Participant> participants = new HashSet<>();
        participants.add(p);
        Set<Event> events = new HashSet<>();
        when(event.getParticipants()).thenReturn(participants);
        when(p.getIdPart()).thenReturn(1);
        when(participantRepository.findById(1)).thenReturn(Optional.of(p));
        when(p.getEvents()).thenReturn(events);
        when(eventRepository.save(event)).thenReturn(event);

        eventServices.addAffectEvenParticipant(event);

        assertTrue(events.contains(event));
        verify(eventRepository).save(event);
    }

    @Test
    void addAffectLog_eventSansLogistics_creeLEnsembleEtSauvegarde() {
        Event event = mock(Event.class);
        Logistics logistics = mock(Logistics.class);
        when(eventRepository.findByDescription("Conference")).thenReturn(event);
        when(event.getLogistics()).thenReturn(null);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        Logistics result = eventServices.addAffectLog(logistics, "Conference");

        assertSame(logistics, result);
        verify(event).setLogistics(any());
        verify(eventRepository).save(event);
    }

    @Test
    void addAffectLog_eventAvecLogistics_ajouteSansSauvegarderEvent() {
        Event event = mock(Event.class);
        Logistics logistics = mock(Logistics.class);
        Set<Logistics> existing = new HashSet<>();
        when(eventRepository.findByDescription("Conference")).thenReturn(event);
        when(event.getLogistics()).thenReturn(existing);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        eventServices.addAffectLog(logistics, "Conference");

        assertTrue(existing.contains(logistics));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void getLogisticsDates_aucunEvent_retourneListeVide() {
        LocalDate d1 = LocalDate.of(2026, 1, 1);
        LocalDate d2 = LocalDate.of(2026, 12, 31);
        when(eventRepository.findByDateDebutBetween(d1, d2)).thenReturn(Collections.emptyList());

        List<Logistics> result = eventServices.getLogisticsDates(d1, d2);

        assertTrue(result.isEmpty());
    }

    @Test
    void getLogisticsDates_eventSansLogistics_retourneNull() {
        LocalDate d1 = LocalDate.of(2026, 1, 1);
        LocalDate d2 = LocalDate.of(2026, 12, 31);
        Event event = mock(Event.class);
        when(eventRepository.findByDateDebutBetween(d1, d2)).thenReturn(Collections.singletonList(event));
        when(event.getLogistics()).thenReturn(new HashSet<>());

        assertNull(eventServices.getLogisticsDates(d1, d2));
    }

    @Test
    void getLogisticsDates_retourneSeulementLesLogisticsReservees() {
        LocalDate d1 = LocalDate.of(2026, 1, 1);
        LocalDate d2 = LocalDate.of(2026, 12, 31);
        Event event = mock(Event.class);
        Logistics reservee = mock(Logistics.class);
        Logistics libre = mock(Logistics.class);
        when(reservee.isReserve()).thenReturn(true);
        when(libre.isReserve()).thenReturn(false);
        when(event.getLogistics()).thenReturn(new HashSet<>(Arrays.asList(reservee, libre)));
        when(eventRepository.findByDateDebutBetween(d1, d2)).thenReturn(Collections.singletonList(event));

        List<Logistics> result = eventServices.getLogisticsDates(d1, d2);

        assertEquals(1, result.size());
        assertSame(reservee, result.get(0));
    }

    @Test
    void calculCout_calculeLeCoutDesLogisticsReservees() {
        Event event = mock(Event.class);
        Logistics logistics = mock(Logistics.class);
        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(Collections.singletonList(event));
        when(event.getDescription()).thenReturn("Conference");
        when(event.getLogistics()).thenReturn(Collections.singleton(logistics));
        when(logistics.isReserve()).thenReturn(true);
        when(logistics.getPrixUnit()).thenReturn(10f);
        when(logistics.getQuantite()).thenReturn(3);

        eventServices.calculCout();

        verify(event).setCout(30f);
        verify(eventRepository).save(event);
    }

    @Test
    void calculCout_aucunEvent_neSauvegardeRien() {
        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(Collections.emptyList());

        eventServices.calculCout();

        verify(eventRepository, never()).save(any());
    }
}