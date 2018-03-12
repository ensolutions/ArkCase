package com.armedia.acm.services.dataaccess.model.test;

import static org.easymock.EasyMock.*;

import com.armedia.acm.data.service.AcmDataService;
import com.armedia.acm.services.dataaccess.annotations.DecoratedAssignedObjectParticipantAspect;
import com.armedia.acm.services.dataaccess.service.impl.AcmAssignedObjectBusinessRule;
import com.armedia.acm.services.participants.model.AcmParticipant;
import com.armedia.acm.services.participants.model.DecoratedAssignedObjectParticipants;

import org.aspectj.lang.ProceedingJoinPoint;
import org.easymock.EasyMockRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(EasyMockRunner.class)
public class DecoratedParticipantAnnotationTest
{

    private AcmDataService springAcmDataService;
    private AcmAssignedObjectBusinessRule assignmentBusinessRule;
    private DecoratedAssignedObjectParticipantAspect decoratedParticipantAspect;
    private DataAccessDao dataAccessDao;

    private ProceedingJoinPoint pjp;
    private DecoratedAssignedObjectParticipants decoratedAssignedObjectParticipants;

    // Additional Assignable Object Arguments;
    private String objectType = "DATA-ACCESS-TEST";
    private Long objectId = 101L;

    @Before
    public void setUp() throws Throwable
    {
        springAcmDataService = createMock(AcmDataService.class);
        dataAccessDao = createMock(DataAccessDao.class);

        assignmentBusinessRule = new AcmAssignedObjectBusinessRule();
        decoratedParticipantAspect = new DecoratedAssignedObjectParticipantAspect();
        decoratedAssignedObjectParticipants = createMock(DecoratedAssignedObjectParticipants.class);
        pjp = createMock(ProceedingJoinPoint.class);
        // expect(pjp.proceed()).andReturn(new DataAccessAssignedObject());
        // DataAccessAssignedObject test = (DataAccessAssignedObject) pjp.proceed();
        // Object objecttest = pjp.proceed();
        Resource ruleFolder = new ClassPathResource("/rules");
        String ruleFolderPath = ruleFolder.getFile().getCanonicalPath();
        assignmentBusinessRule.setRuleFileLocation(ruleFolderPath);
        assignmentBusinessRule.setRuleSpreadsheetFilename("drools-assigned-object-test-rules.xlsx");
        assignmentBusinessRule.afterPropertiesSet();
        decoratedParticipantAspect.setSpringAcmDataService(springAcmDataService);
        decoratedParticipantAspect.setAssignmentBusinessRule(assignmentBusinessRule);

    }

    @Test
    public void AssignableObjectDecorationWithoutParticipantsTest() throws Throwable
    {
        DataAccessAssignedObject testObject = new DataAccessAssignedObject();
        expect(pjp.proceed()).andReturn(testObject);
        replay(pjp);
        DataAccessAssignedObject decorated = (DataAccessAssignedObject) decoratedParticipantAspect.aroundDecoratingMethod(pjp,
                decoratedAssignedObjectParticipants);

        Assert.assertEquals(decorated.getParticipants().size(), 0);
    }

    @Test
    public void AssignableObjectDecorationWithParticipantsTest() throws Throwable
    {
        DataAccessAssignedObject testObject = new DataAccessAssignedObject();
        testObject.setParticipants(getDefaultParticipants());
        testObject.setStatus("DRAFT");
        expect(pjp.proceed()).andReturn(testObject);
        replay(pjp);
        DataAccessAssignedObject decorated = (DataAccessAssignedObject) decoratedParticipantAspect.aroundDecoratingMethod(pjp,
                decoratedAssignedObjectParticipants);

        validateParticipants(decorated.getParticipants(), decorated.getCreator());

    }

    @Test
    public void ParticipantsListDecorationTest() throws Throwable
    {
        DataAccessAssignedObject testObject = new DataAccessAssignedObject();
        List<AcmParticipant> participants = getDefaultParticipants();
        testObject.setParticipants(participants);
        SetupTestObjectReturnMethod(testObject, participants);
        List<AcmParticipant> decorated = (List<AcmParticipant>) decoratedParticipantAspect.aroundDecoratingMethod(pjp,
                decoratedAssignedObjectParticipants);

        validateParticipants(decorated, testObject.getCreator());

    }

    @Test
    public void ParticipantsListDecorationWithoutParticipantsTest() throws Throwable
    {
        DataAccessAssignedObject testObject = new DataAccessAssignedObject();
        List<AcmParticipant> participants = new ArrayList<>();
        testObject.setParticipants(participants);
        SetupTestObjectReturnMethod(testObject, participants);
        List<AcmParticipant> decorated = (List<AcmParticipant>) decoratedParticipantAspect.aroundDecoratingMethod(pjp,
                decoratedAssignedObjectParticipants);

        Assert.assertEquals(decorated.size(), 0);
    }

    @Test
    public void ParticipantsListDecorationWithInvalidParentObject() throws Throwable
    {
        List<AcmParticipant> participants = getDefaultParticipants();
        SetupTestObjectReturnMethod(null, participants);
        List<AcmParticipant> decorated = (List<AcmParticipant>) decoratedParticipantAspect.aroundDecoratingMethod(pjp,
                decoratedAssignedObjectParticipants);

        Assert.assertEquals(decorated.size(), participants.size());
    }

    private void SetupTestObjectReturnMethod(DataAccessAssignedObject object, List<AcmParticipant> participants) throws Throwable
    {
        Object[] args = new Object[2];
        args[0] = this.objectType;
        args[1] = this.objectId;
        expect(pjp.proceed()).andReturn(participants);
        expect(pjp.getArgs()).andReturn(args);
        replay(pjp);
        expect(decoratedAssignedObjectParticipants.objectTypeIndex()).andReturn(0);
        expect(decoratedAssignedObjectParticipants.objectIdIndex()).andReturn(1);
        expect(decoratedAssignedObjectParticipants.objectId()).andReturn(-1);
        expect(decoratedAssignedObjectParticipants.objectType()).andReturn("");
        replay(decoratedAssignedObjectParticipants);
        expect(dataAccessDao.find(this.objectId)).andReturn(object);
        replay(dataAccessDao);
        expect(springAcmDataService.getDaoByObjectType(this.objectType)).andReturn(dataAccessDao);
        replay(springAcmDataService);
    }

    private void validateParticipants(List<AcmParticipant> participants, String creator)
    {
        for (int i = 0; i < participants.size(); i++)
        {
            AcmParticipant participant = participants.get(i);
            if (participant.getParticipantType().equals("*"))
            {
                // Participant with type * should only have editable user
                Assert.assertTrue(participant.isEditableUser());
                Assert.assertFalse(participant.isEditableType());
                Assert.assertFalse(participant.isDeletable());
            }
            else if (participant.getParticipantType().equals("reader") && participant.getParticipantLdapId().equals(creator))
            {
                // Participant reader that is creator cannot be deleted or edited
                Assert.assertFalse(participant.isDeletable());
                Assert.assertFalse(participant.isEditableType());
                Assert.assertFalse(participant.isEditableUser());
            }
            else if (participant.getParticipantType().equals("owning group"))
            {
                // Participan owning group can only change its user
                Assert.assertFalse(participant.isDeletable());
                Assert.assertFalse(participant.isEditableType());
                Assert.assertTrue(participant.isEditableUser());
            }
        }
    }

    private List<AcmParticipant> getDefaultParticipants()
    {
        List<AcmParticipant> participants = new ArrayList<>();

        AcmParticipant participantStar = new AcmParticipant();
        participantStar.setId(1L);
        participantStar.setParticipantLdapId(UUID.randomUUID().toString());
        participantStar.setParticipantType("*");
        participantStar.setObjectType(this.objectType);
        participantStar.setObjectId(this.objectId);
        participants.add(participantStar);

        AcmParticipant participantOwningGroup = new AcmParticipant();
        participantOwningGroup.setId(2L);
        participantOwningGroup.setParticipantLdapId(UUID.randomUUID().toString());
        participantOwningGroup.setParticipantType("owning group");
        participantOwningGroup.setObjectType(this.objectType);
        participantOwningGroup.setObjectId(this.objectId);
        participants.add(participantOwningGroup);

        AcmParticipant participantReader = new AcmParticipant();
        participantReader.setId(3L);
        participantReader.setParticipantLdapId("TEST-CREATOR");
        participantReader.setObjectType(this.objectType);
        participantReader.setParticipantType("reader");
        participantReader.setObjectId(this.objectId);
        participants.add(participantReader);

        return participants;
    }

}
