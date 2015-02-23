/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.bps.integration.tests.bpmn;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpmn.ActivitiRestClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

import java.io.File;
import java.io.IOException;


public class UserTaskTestCase extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(UserTaskTestCase.class);

    @Test(groups = {"wso2.bps.test.usertasks"}, description = "User Task Test", priority = 1, singleThreaded = true)
    public void UserTaskTestCase() throws Exception {
        init();
        ActivitiRestClient tester = new ActivitiRestClient(bpsServer.getInstance().getPorts().get("http"), bpsServer.getInstance().getHosts().get("default"));
        //deploying Package
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + File.separator
                          + BPMNTestConstants.DIR_ARTIFACTS + File.separator
                          + BPMNTestConstants.DIR_BPMN + File.separator + "AssigneeIsEmpty.bar";

        String fileName = "AssigneeIsEmpty.bar";
        String[] deploymentResponse ={};


        try {
            deploymentResponse = tester.deployBPMNPackage(filePath, fileName);
            Assert.assertTrue("Deployment Successful", deploymentResponse[0].contains(BPMNTestConstants.CREATED));
        }catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }

        try {
            String[] deploymentCheckResponse = tester.getDeploymentInfoById(deploymentResponse[1]);
            Assert.assertTrue("Deployment Present", deploymentCheckResponse[2].contains(fileName));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }


        //Acquiring Process Definition ID to start Process Instance
        String[] definitionResponse = new String[0];
        try {
            definitionResponse = tester.findProcessDefinitionInfoById(deploymentResponse[1]);
            Assert.assertTrue("Search Success", definitionResponse[0].contains(BPMNTestConstants.OK));
        }catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }


        //Starting and Verifying Process Instance
        String[] processInstanceResponse = new String[0];
        try {
            processInstanceResponse = tester.startProcessInstanceByDefintionID(definitionResponse[1]);
            Assert.assertTrue("Process Instance Started", processInstanceResponse[0].contains(BPMNTestConstants.CREATED));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }


        try {
            String searchResponse = tester.searchProcessInstanceByDefintionID(definitionResponse[1]);
            Assert.assertTrue("Process Instance Present", searchResponse.contains(BPMNTestConstants.OK));
        }catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        }

        tester.waitForTaskGeneration();

        //Acquiring TaskID to perform Task Related Tests
        String[] taskResponse = new String[0];
        try {
            taskResponse = tester.findTaskIdByProcessInstanceID(processInstanceResponse[1]);
            Assert.assertTrue("Task ID Acquired", taskResponse[0].contains(BPMNTestConstants.OK));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }


        //Claiming a User Task
        try {
            String claimResponse = tester.claimTaskByTaskId(taskResponse[1]);
            Assert.assertTrue("User has claimed Task", claimResponse.contains(BPMNTestConstants.NO_CONTENT));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        }

        String currentAssignee = null;
        try {
            currentAssignee = tester.getAssigneeByTaskId(taskResponse[1]);
            Assert.assertTrue("User has been assigned", currentAssignee.contains(BPMNTestConstants.USER_CLAIM));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }


        //Delegating a User Task
        try {
            String delegateStatus = tester.delegateTaskByTaskId(taskResponse[1]);
            Assert.assertTrue("Task has been delegated", delegateStatus.contains(BPMNTestConstants.NO_CONTENT));
        }catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        }

        try {
            currentAssignee = tester.getAssigneeByTaskId(taskResponse[1]);
            Assert.assertTrue("Testing Delegated User Matches Assignee", currentAssignee.equals(BPMNTestConstants.USER_DELEGATE));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }


        //Commenting on a user task
        String[] commentResponse = new String[0];
        try {
            commentResponse = tester.addNewCommentOnTaskByTaskId(taskResponse[1], BPMNTestConstants.COMMENT_MESSAGE);
            Assert.assertTrue("Comment Has been added", commentResponse[0].contains(BPMNTestConstants.CREATED));
            Assert.assertTrue("Comment is visible", commentResponse[1].contains(BPMNTestConstants.COMMENT_MESSAGE));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }


        try {
            String validateComment = tester.getCommentByTaskIdAndCommentId(taskResponse[1], commentResponse[2]);
            Assert.assertTrue("Validating Comment Existence", validateComment.contains(BPMNTestConstants.COMMENT_MESSAGE));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }


        //resolving a User Task
        try {
            String status = tester.resolveTaskByTaskId(taskResponse[1]);
            String stateValue = tester.getDelegationsStateByTaskId(taskResponse[1]);
            Assert.assertTrue("Checking Delegation State", stateValue.equals("resolved"));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }


        //Deleting a Process Instance
        try {
            String deleteStatus = tester.deleteProcessInstanceByID(processInstanceResponse[1]);
            Assert.assertTrue("Process Instance Removed", deleteStatus.contains(BPMNTestConstants.NO_CONTENT));
        }catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        }

        try {
            String deleteCheck = tester.validateProcessInstanceById(definitionResponse[1]);
            Assert.assertTrue("Process Instance Removed Check", deleteCheck.contains(BPMNTestConstants.NOT_AVAILABLE));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }


        //Deleting the Deployment
        try {
            String undeployStatus = tester.unDeployBPMNPackage(deploymentResponse[1]);
            Assert.assertTrue("Package UnDeployed", undeployStatus.contains(BPMNTestConstants.NO_CONTENT));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        }

        try {
            String[] unDeployCheck = tester.getDeploymentInfoById(deploymentResponse[1]);
            Assert.assertTrue("Package UnDeployment", unDeployCheck[0].equals(BPMNTestConstants.NOT_AVAILABLE));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }


    }


}