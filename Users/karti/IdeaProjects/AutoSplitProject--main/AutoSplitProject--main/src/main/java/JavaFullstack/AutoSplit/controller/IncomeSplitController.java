package JavaFullstack.AutoSplit.controller;

import JavaFullstack.AutoSplit.model.IncomeSplit;
import JavaFullstack.AutoSplit.model.User;
import JavaFullstack.AutoSplit.service.IncomeSplitService;
import JavaFullstack.AutoSplit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/income-splits")
public class IncomeSplitController {

    @Autowired
    private IncomeSplitService incomeSplitService;

    @Autowired
    private UserService userService;

    /**
     * Get splits by userId - only if the logged-in user matches the requested userId
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getIncomeSplits(@PathVariable Long userId, Principal principal) {
        Optional<User> currentUser = userService.findByEmail(principal.getName());

        if (currentUser.isEmpty() || !currentUser.get().getId().equals(userId)) {
            return ResponseEntity.status(403).body("You are not allowed to view this user's income splits");
        }

        return ResponseEntity.ok(incomeSplitService.getIncomeSplitsByUser(currentUser.get()));
    }

    /**
     * Save or update splits - only if the logged-in user matches the requested userId
     */
    @PostMapping("/{userId}")
    public ResponseEntity<?> saveIncomeSplits(
            @PathVariable Long userId,
            @RequestBody List<IncomeSplit> splits,
            Principal principal) {

        Optional<User> currentUser = userService.findByEmail(principal.getName());

        if (currentUser.isEmpty() || !currentUser.get().getId().equals(userId)) {
            return ResponseEntity.status(403).body("You can only update your own income splits");
        }

        List<IncomeSplit> savedSplits = incomeSplitService.saveIncomeSplits(currentUser.get(), splits);
        return ResponseEntity.ok(savedSplits);
    }
}
