package com.pawanputra.bos.platform.system;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Response of {@code GET /api/v1/system/info}.
 *
 * @param application human-readable application name
 * @param version     build version
 * @param serverTime  current server time, ISO-8601 UTC
 */
@Schema(description = "Identity and current time of the running application")
public record SystemInfoResponse(String application, String version, Instant serverTime) {
}
