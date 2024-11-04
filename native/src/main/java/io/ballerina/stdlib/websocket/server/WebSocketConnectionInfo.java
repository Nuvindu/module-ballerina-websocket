/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.stdlib.websocket.server;

import io.ballerina.runtime.api.values.BObject;
import io.ballerina.stdlib.http.transport.contract.websocket.WebSocketConnection;
import io.ballerina.stdlib.websocket.WebSocketConstants;
import io.ballerina.stdlib.websocket.WebSocketService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This class has WebSocket connection info for both the client and the server. Includes details
 * needed to dispatch a resource after a successful handshake.
 */
public class WebSocketConnectionInfo {

    private final WebSocketService webSocketService;
    private final BObject webSocketEndpoint;
    private final WebSocketConnection webSocketConnection;
    private StringAggregator stringAggregator = null;
    private ByteArrAggregator byteArrAggregator = null;
    private List<CompletableFuture<Object>> callbacks = new ArrayList<>();

    /**
     * @param webSocketService    can be the WebSocketServerService or WebSocketService
     * @param webSocketConnection can be the client or server connection or null if the connection hasn't been made.
     * @param webSocketEndpoint   can be the WebSocketCaller or the WebSocketClient
     */
    public WebSocketConnectionInfo(WebSocketService webSocketService, WebSocketConnection webSocketConnection,
            BObject webSocketEndpoint) {
        this.webSocketService = webSocketService;
        this.webSocketConnection = webSocketConnection;
        this.webSocketEndpoint = webSocketEndpoint;
    }

    public WebSocketService getService() {
        return webSocketService;
    }

    public BObject getWebSocketEndpoint() {
        return webSocketEndpoint;
    }

    public WebSocketConnection getWebSocketConnection() throws IllegalAccessException {
        if (webSocketConnection != null) {
            return webSocketConnection;
        } else {
            throw new IllegalAccessException(WebSocketConstants.WEBSOCKET_CONNECTION_FAILURE);
        }
    }

    public StringAggregator createIfNullAndGetStringAggregator() {
        if (stringAggregator == null) {
            stringAggregator = new StringAggregator();
        }
        return stringAggregator;
    }

    public ByteArrAggregator createIfNullAndGetByteArrAggregator() {
        if (byteArrAggregator == null) {
            byteArrAggregator = new ByteArrAggregator();
        }
        return byteArrAggregator;
    }

    /**
     * A string aggregator to handle string aggregation for data binding during onTextMessage resource dispatching. The
     * aggregation is done in the ConnectionInfo class because the strings specific to a particular connection needs to
     * be aggregated.
     */
    public static class StringAggregator {
        private StringAggregator() {

        }

        private StringBuilder aggregateStrBuilder = new StringBuilder();

        public String getAggregateString() {
            return aggregateStrBuilder.toString();
        }

        public void appendAggregateString(String aggregateString) {
            aggregateStrBuilder.append(aggregateString);
        }

        public void resetAggregateString() {
            aggregateStrBuilder = new StringBuilder();
        }
    }

    /**
     * A byte array aggregator to handle byte array aggregation until the final frame is received. The aggregation
     * is done in the ConnectionInfo class because the byte arrays specific to a particular connection needs to
     * be aggregated.
     */
    public static class ByteArrAggregator {
        private ByteArrAggregator() {

        }

        private ByteArrayOutputStream aggregateArr = new ByteArrayOutputStream();

        public byte[] getAggregateByteArr() {
            return aggregateArr.toByteArray();
        }

        public void appendAggregateArr(byte[] aggregateByteArr) throws IOException {
            this.aggregateArr.write(aggregateByteArr);
        }

        public void resetAggregateByteArr() {
            this.aggregateArr = new ByteArrayOutputStream();
        }
    }

    public void addCallback(CompletableFuture<Object> callback) {
        callbacks.add(callback);
    }

    public List<CompletableFuture<Object>> getCallbacks() {
        return callbacks;
    }
}
