package ZgazeniSendvic.Server_Back_ISS.repository.spec;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import org.springframework.data.jpa.domain.Specification;

public class AccountSpecs {

    public static Specification<Account> search(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("email")), like),
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("lastName")), like),
                cb.like(cb.lower(root.get("phoneNumber")), like)
        );
    }

    public static Specification<Account> confirmed(Boolean confirmed) {
        if (confirmed == null) return null;
        return (root, query, cb) -> cb.equal(root.get("confirmed"), confirmed);
    }

    /**
     * If you use SINGLE_TABLE inheritance with @DiscriminatorColumn(name="account_type"),
     * you can filter by discriminator using root.type() with subclasses
     * OR map the discriminator column as a read-only field.
     *
     * Easiest: filter by Java type if you have subclasses Admin/User/Driver.
     */
    public static Specification<Account> type(String type) {
        if (type == null || type.isBlank()) return null;
        String t = type.trim().toUpperCase();

        return (root, query, cb) -> switch (t) {
            case "ADMIN" -> cb.equal(root.type(), ZgazeniSendvic.Server_Back_ISS.model.Admin.class);
            case "DRIVER" -> cb.equal(root.type(), ZgazeniSendvic.Server_Back_ISS.model.Driver.class);
            case "USER" -> cb.equal(root.type(), ZgazeniSendvic.Server_Back_ISS.model.User.class);
            default -> null; // unknown -> no filter
        };
    }
}
