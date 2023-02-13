/*
 *    Copyright (c) 2020, VRAI Labs and/or its affiliates. All rights reserved.
 *
 *    This software is licensed under the Apache License, Version 2.0 (the
 *    "License") as published by the Apache Software Foundation.
 *
 *    You may not use this file except in compliance with the License. You may
 *    obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */

package io.supertokens.webserver.api.session;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.supertokens.Main;
import io.supertokens.config.Config;
import io.supertokens.pluginInterface.RECIPE_ID;
import io.supertokens.pluginInterface.exceptions.StorageQueryException;
import io.supertokens.pluginInterface.exceptions.StorageTransactionLogicException;
import io.supertokens.signingkeys.SigningKeys;
import io.supertokens.signingkeys.SigningKeys.KeyInfo;
import io.supertokens.utils.Utils;
import io.supertokens.webserver.WebserverAPI;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class HandshakeAPI extends WebserverAPI {
    private static final long serialVersionUID = -3647598432179106404L;

    public HandshakeAPI(Main main) {
        super(main, RECIPE_ID.SESSION.toString());
    }

    @Override
    public String getPath() {
        return "/recipe/handshake";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (super.getVersionFromRequest(req).equals("2.18")) {
            super.sendTextResponse(404, "Not found", resp);
            return;
        }
        try {
            JsonObject result = new JsonObject();
            result.addProperty("status", "OK");

            result.addProperty("jwtSigningPublicKey",
                    new Utils.PubPriKey(SigningKeys.getInstance(main).getLatestIssuedDynamicKey().value).publicKey);
            result.addProperty("jwtSigningPublicKeyExpiryTime",
                    SigningKeys.getInstance(main).getDynamicSigningKeyExpiryTime());

            if (!super.getVersionFromRequest(req).equals("2.7") && !super.getVersionFromRequest(req).equals("2.8")) {
                List<KeyInfo> keys = SigningKeys.getInstance(main).getDynamicKeys();
                JsonArray jwtSigningPublicKeyListJSON = Utils.keyListToJson(keys);
                result.add("jwtSigningPublicKeyList", jwtSigningPublicKeyListJSON);
            }

            result.addProperty("accessTokenBlacklistingEnabled", Config.getConfig(main).getAccessTokenBlacklisting());
            result.addProperty("accessTokenValidity", Config.getConfig(main).getAccessTokenValidity());
            result.addProperty("refreshTokenValidity", Config.getConfig(main).getRefreshTokenValidity());
            super.sendJsonResponse(200, result, resp);
        } catch (StorageQueryException | StorageTransactionLogicException e) {
            throw new ServletException(e);
        }
    }
}
