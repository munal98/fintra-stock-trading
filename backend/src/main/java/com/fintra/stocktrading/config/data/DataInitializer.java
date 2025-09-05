package com.fintra.stocktrading.config.data;

import com.fintra.stocktrading.model.entity.*;
import com.fintra.stocktrading.model.enums.*;
import com.fintra.stocktrading.repository.*;
import com.fintra.stocktrading.service.EquityDataInitializationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CashBalanceRepository cashBalanceRepository;
    private final EquityRepository equityRepository;
    private final EquityStockRepository equityStockRepository;
    private final OtherInstitutionRepository otherInstitutionRepository;

    private final PasswordEncoder passwordEncoder;
    private final EquityDataInitializationService equityDataInitializationService;
    private final TransactionTemplate tx;
    private final EquityPriceHistoryRepository equityPriceHistoryRepository;

    private static final BigDecimal DEFAULT_FREE_BALANCE = new BigDecimal("50000.00");
    private static final int DEFAULT_STOCK_QTY = 500;
    private static final String THYAO_CODE = "THYAO.E";

    @Override
    public void run(String... args) {
        long t0 = System.currentTimeMillis();
        log.info("[Seed] Starting...");

        initEquityDataFromApiOrFallback();

        tx.executeWithoutResult(s -> {
            seedUsers();
            seedCustomersAccountsAndPositions();
            initOtherInstitutions();
        });

        log.info("[Seed] Completed in {} ms.", (System.currentTimeMillis() - t0));
    }

    private void initEquityDataFromApiOrFallback() {
        try {
            equityDataInitializationService.initializeEquityData();
            log.info("[Seed] Equity data initialized via external API.");
        } catch (Exception e) {
            log.warn("[Seed] API equity init failed, using fallback set (10 BIST). Reason: {}", e.getMessage());
            initFallbackEquities();
        }
    }

    private void initFallbackEquities() {
        List<String[]> fallback = Arrays.asList(
                new String[]{"THYAO.E", "Türk Hava Yolları A.O.", "THYAO", "XIST", "TR", "XU100,XUHIZ"},
                new String[]{"GARAN.E", "Türkiye Garanti Bankası A.Ş.", "GARAN", "XIST", "TR", "XU100,XBANK"},
                new String[]{"ASELS.E", "Aselsan Elektronik Sanayi ve Ticaret A.Ş.", "ASELS", "XIST", "TR", "XU100,XTECH"},
                new String[]{"TUPRS.E", "Tüpraş-Türkiye Petrol Rafinerileri A.Ş.", "TUPRS", "XIST", "TR", "XU100,XKMYA,XKURY"},
                new String[]{"BIMAS.E", "BİM Birleşik Mağazalar A.Ş.", "BIMAS", "XIST", "TR", "XU100"},
                new String[]{"EREGL.E", "Ereğli Demir ve Çelik Fabrikaları T.A.Ş.", "EREGL", "XIST", "TR", "XU100,XUSIN"},
                new String[]{"KCHOL.E", "Koç Holding A.Ş.", "KCHOL", "XIST", "TR", "XU100,XHOLD"},
                new String[]{"AKBNK.E", "Akbank T.A.Ş.", "AKBNK", "XIST", "TR", "XU100,XBANK"},
                new String[]{"YKBNK.E", "Yapı ve Kredi Bankası A.Ş.", "YKBNK", "XIST", "TR", "XU100,XBANK"},
                new String[]{"SAHOL.E", "Sabancı Holding A.Ş.", "SAHOL", "XIST", "TR", "XU100,XHOLD"}
        );

        for (String[] d : fallback) {
            boolean participation = d[5].contains("XK");
            Optional<Equity> existing = equityRepository.findByEquityCode(d[2])
                    .or(() -> equityRepository.findByTicker(d[0]));
            if (existing.isEmpty()) {
                Equity e = Equity.builder()
                        .ticker(d[0])
                        .equityCode(d[2])
                        .equityName(d[1])
                        .market(d[3])
                        .country(d[4])
                        .indexInfo(d[5])
                        .participation(participation)
                        .equityType(EquityType.STOCK)
                        .build();
                equityRepository.save(e);
            }
        }
        log.info("[Seed] Fallback equities ensured (10).");
    }

    private void seedUsers() {
        upsertUser("admin@fintra.com.tr", "Ahmet", "Yılmaz", "Admin123!", Role.ROLE_ADMIN);
        upsertUser("zeynep.demir@fintra.com.tr", "Zeynep", "Demir", "Trader123!", Role.ROLE_TRADER);
        upsertUser("mehmet.can@fintra.com.tr", "Mehmet", "Can", "Analyst123!", Role.ROLE_ANALYST);
        log.info("[Seed] Users ensured (3).");
    }

    private void upsertUser(String email, String firstName, String lastName, String password, Role role) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            log.debug("User already exists: {}", email);
            return;
        }

        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        userRepository.save(user);
        log.info("Created {} user: {} {}", role.name().replace("ROLE_", "").toLowerCase(), firstName, lastName);
    }

    private void seedCustomersAccountsAndPositions() {
        User trader = userRepository.findByEmail("zeynep.demir@fintra.com.tr").orElse(null);

        List<CustomerSeed> seeds = List.of(
                new CustomerSeed("Ali",   "Yılmaz",  AccountType.INDIVIDUAL),
                new CustomerSeed("Ayşe",  "Kaya",    AccountType.INDIVIDUAL),
                new CustomerSeed("Mehmet","Demir",   AccountType.INDIVIDUAL),
                new CustomerSeed("Fatma", "Özkan",   AccountType.INDIVIDUAL),
                new CustomerSeed("Emre",  "Koç",     AccountType.INDIVIDUAL),
                new CustomerSeed("Selin", "Arslan",  AccountType.INDIVIDUAL),
                new CustomerSeed("Hasan", "Çelik",   AccountType.INDIVIDUAL),
                new CustomerSeed("Zehra", "Şahin",   AccountType.INDIVIDUAL)
        );

        Equity thyao = getOrCreateEquity(THYAO_CODE, "Türk Hava Yolları A.O.");

        BigDecimal thyaoAvgCost = resolveLastClosePrice(THYAO_CODE).orElse(new BigDecimal("100.00"));

        int ensured = 0;
        for (int i = 0; i < seeds.size(); i++) {
            CustomerSeed cs = seeds.get(i);
            final int customerIndex = i;
            String email = buildGmail(cs.firstName, cs.lastName, i + 1);

            String identity = cs.accountType == AccountType.INDIVIDUAL ?
                    randomNumericString(11) : randomNumericString(10);

            Customer customer = customerRepository.findByEmail(email).orElseGet(() -> {
                Customer c = Customer.builder()
                        .user(trader)
                        .firstName(cs.firstName)
                        .lastName(cs.lastName)
                        .email(email)
                        .identityNumber(identity)
                        .tradingPermission(customerIndex < 6 ? TradingPermission.FULL : TradingPermission.PARTICIPATION_ONLY)
                        .tradingEnabled(true)
                        .build();
                return customerRepository.save(c);
            });

            Account account = accountRepository.findByCustomer(customer)
                    .stream().findFirst().orElseGet(() -> {
                        Account a = Account.builder()
                                .customer(customer)
                                .accountType(cs.accountType)
                                .build();
                        return accountRepository.save(a);
                    });

            CashBalance cb = cashBalanceRepository.findByAccount(account).orElse(null);
            if (cb == null) {
                cb = CashBalance.builder()
                        .account(account)
                        .freeBalance(DEFAULT_FREE_BALANCE)
                        .blockedBalance(BigDecimal.ZERO)
                        .build();
            } else {
                cb.setFreeBalance(DEFAULT_FREE_BALANCE);
                cb.setBlockedBalance(BigDecimal.ZERO);
            }
            cashBalanceRepository.save(cb);

            EquityStock stock = equityStockRepository.findByAccountAndEquity(account, thyao).orElse(null);
            if (stock == null) {
                stock = EquityStock.builder()
                        .account(account)
                        .equity(thyao)
                        .freeQuantity(DEFAULT_STOCK_QTY)
                        .blockedQuantity(0)
                        .avgCost(thyaoAvgCost)
                        .build();
            } else {
                stock.setFreeQuantity(DEFAULT_STOCK_QTY);
                stock.setBlockedQuantity(0);
                if (stock.getAvgCost() == null) {
                    stock.setAvgCost(thyaoAvgCost);
                }
            }
            equityStockRepository.save(stock);

            ensured++;
        }

        log.info("[Seed] Customers/Accounts/Positions ensured ({}).", ensured);
    }

    private record CustomerSeed(String firstName, String lastName, AccountType accountType) {
    }

    private String buildGmail(String firstName, String lastName, int index) {
        String fn = sanitize(firstName);
        String ln = sanitize(lastName == null ? "" : lastName);
        String base = ln.isBlank() ? fn : fn + "." + ln;
        return base + index + "@gmail.com";
    }

    private String sanitize(String s) {
        if (s == null) return "";
        String cleaned = s.toLowerCase(Locale.ROOT)
                .replace("ç","c").replace("ğ","g").replace("ı","i")
                .replace("ö","o").replace("ş","s").replace("ü","u")
                .replaceAll("[^a-z0-9]+", ".");
        cleaned = cleaned.replaceAll("\\.+", ".");
        cleaned = cleaned.replaceAll("^\\.|\\.$", "");
        if (cleaned.isBlank()) cleaned = "user";
        return cleaned;
    }

    private String randomNumericString(int len) {
        Random r = new Random();
        StringBuilder sb = new StringBuilder(len);
        sb.append(r.nextInt(9) + 1);
        for (int i = 1; i < len; i++) sb.append(r.nextInt(10));
        return sb.toString();
    }

    private void initOtherInstitutions() {
        List<String> banks = Arrays.asList(
                "Türkiye İş Bankası A.Ş.",
                "Türkiye Cumhuriyeti Ziraat Bankası A.Ş.",
                "Türkiye Halk Bankası A.Ş.",
                "Yapı ve Kredi Bankası A.Ş.",
                "Akbank T.A.Ş."
        );

        for (String name : banks) {
            boolean exists = otherInstitutionRepository.findAll().stream()
                    .anyMatch(b -> name.equalsIgnoreCase(b.getName()));
            if (!exists) {
                otherInstitutionRepository.save(
                        OtherInstitution.builder().name(name).build()
                );
            }
        }
        log.info("[Seed] Other institutions ensured.");
    }

    private Optional<BigDecimal> resolveLastClosePrice(String equityCodeOrTicker) {
        Optional<Equity> equityOpt = equityRepository.findByEquityCode(equityCodeOrTicker)
                .or(() -> equityRepository.findByTicker(equityCodeOrTicker));

        if (equityOpt.isEmpty()) {
            return Optional.empty();
        }

        List<EquityPriceHistory> priceHistory = equityPriceHistoryRepository
                .findByEquityOrderByDataDateDesc(equityOpt.get());

        return priceHistory.isEmpty()
                ? Optional.empty()
                : Optional.ofNullable(priceHistory.get(0).getClosePrice());
    }

    private Equity getOrCreateEquity(String code, String name) {
        return equityRepository.findByEquityCode(code)
                .or(() -> equityRepository.findByTicker(code))
                .orElseGet(() -> {
                    Equity eq = Equity.builder()
                            .ticker(code)
                            .equityCode(code)
                            .equityName(name)
                            .market("XIST")
                            .country("TR")
                            .indexInfo("XU100")
                            .participation(false)
                            .equityType(EquityType.STOCK)
                            .build();
                    equityRepository.save(eq);
                    return eq;
                });
    }
}
