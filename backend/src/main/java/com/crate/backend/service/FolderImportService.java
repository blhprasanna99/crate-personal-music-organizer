package com.crate.backend.service;

import com.crate.backend.entity.Track;
import com.crate.backend.repo.TrackRepository;
import com.crate.backend.support.CurrentUser;
import com.crate.backend.web.dto.ImportSummary;
import com.crate.backend.web.dto.ImportSummary.ImportFailure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class FolderImportService {

    private static final Set<String> AUDIO_EXTENSIONS = Set.of(
            "mp3", "m4a", "mp4", "flac", "ogg", "wav", "aiff", "aif"
    );

    private final TrackRepository tracks;
    private final CurrentUser currentUser;

    @Transactional
    public ImportSummary importFolder(String folderPath) throws IOException {
        Path root = Path.of(folderPath);
        if (!Files.isDirectory(root)) {
            throw new IllegalArgumentException("not a directory: " + folderPath);
        }

        Long ownerId = currentUser.id();
        int scanned = 0, imported = 0, skipped = 0;
        List<ImportFailure> errors = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(root)) {
            for (Path path : (Iterable<Path>) walk::iterator) {
                if (!Files.isRegularFile(path)) continue;
                if (!hasAudioExtension(path)) continue;
                scanned++;

                String absolute = path.toAbsolutePath().toString();
                if (tracks.findByOwnerIdAndFilePath(ownerId, absolute).isPresent()) {
                    skipped++;
                    continue;
                }

                try {
                    Track t = readTrack(path.toFile(), ownerId, absolute);
                    tracks.save(t);
                    imported++;
                } catch (Exception ex) {
                    log.warn("failed to import {}: {}", absolute, ex.getMessage());
                    errors.add(new ImportFailure(absolute, ex.getMessage()));
                }
            }
        }

        return new ImportSummary(scanned, imported, skipped, errors.size(), errors);
    }

    private static boolean hasAudioExtension(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        int dot = name.lastIndexOf('.');
        return dot > 0 && AUDIO_EXTENSIONS.contains(name.substring(dot + 1));
    }

    private static Track readTrack(File file, Long ownerId, String absolutePath) throws Exception {
        AudioFile af = AudioFileIO.read(file);
        Tag tag = af.getTag();

        String title  = tag == null ? null : nullIfBlank(tag.getFirst(FieldKey.TITLE));
        String artist = tag == null ? null : nullIfBlank(tag.getFirst(FieldKey.ARTIST));
        String album  = tag == null ? null : nullIfBlank(tag.getFirst(FieldKey.ALBUM));

        Integer durationMs = af.getAudioHeader() == null
                ? null
                : af.getAudioHeader().getTrackLength() * 1000;

        return Track.builder()
                .ownerId(ownerId)
                .filePath(absolutePath)
                .title(title)
                .artist(artist)
                .album(album)
                .durationMs(durationMs)
                .build();
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
