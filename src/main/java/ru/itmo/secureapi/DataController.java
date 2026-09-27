package ru.itmo.secureapi;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

@RestController
@RequestMapping("/api/data")
public class DataController {
    private final JdbcTemplate jdbc;
    private final SimpleJdbcInsert insert;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public DataController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.insert = new SimpleJdbcInsert(jdbc).withTableName("data_item").usingGeneratedKeyColumns("id");
    }

    @GetMapping
    public List<DataItem> list() {
        return jdbc.query("SELECT id, text FROM data_item ORDER BY id",
            (rs, row) -> new DataItem(rs.getLong("id"), HtmlUtils.htmlEscape(rs.getString("text"))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DataItem create(@Valid @RequestBody CreateDataRequest request) {
        Number key = insert.executeAndReturnKey(Map.of("text", request.text()));
        return new DataItem(key.longValue(), HtmlUtils.htmlEscape(request.text()));
    }

    public record CreateDataRequest(@NotBlank @Size(max = 500) String text) {}

    public record DataItem(long id, String text) {}
}
