package com.haan.perfumeshop.config;

import com.haan.perfumeshop.model.User;
import com.haan.perfumeshop.model.Perfume;
import com.haan.perfumeshop.model.PerfumeVariant;
import com.haan.perfumeshop.repository.UserRepository;
import com.haan.perfumeshop.repository.PerfumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PerfumeRepository perfumeRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // 1. Khởi tạo tài khoản Admin mặc định (mã hóa mật khẩu bằng BCrypt)
        if (userRepository.findByEmail("admin@haan.vn").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@haan.vn");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setFullName("Quản trị viên");
            admin.setPhone("0987654321");
            admin.setAddress("Hà Nội, Việt Nam");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("✅ Đã khởi tạo tài khoản Admin mặc định!");
        }

        // 2. Khởi tạo tài khoản Khách hàng mặc định để tiện chạy test
        if (userRepository.findByEmail("user@haan.vn").isEmpty()) {
            User user = new User();
            user.setEmail("user@haan.vn");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setFullName("Khách Hàng Mẫu");
            user.setPhone("0123456789");
            user.setAddress("Hồ Chí Minh, Việt Nam");
            user.setRole("user");
            userRepository.save(user);
            System.out.println("✅ Đã khởi tạo tài khoản Khách hàng mặc định!");
        }

        // 3. Khởi tạo 10 sản phẩm nước hoa tiêu biểu kèm biến thể
        if (perfumeRepository.count() == 0) {
            System.out.println("🌱 Bắt đầu khởi tạo dữ liệu nước hoa mẫu chất lượng cao...");

            // Khai báo các ảnh có sẵn trong thư mục static/uploads
            String imgBleu = "eb59436f-6678-44b2-9656-cf557ff91d49_Bleu de Chane.jpeg";
            String imgPoison = "10000447-7a16-45b3-9a35-f6c2006bff3d_Nước Hoa Poison Girl Dior for women.jpg";
            String imgHermes = "2c1f85f0-6274-44c1-9c0a-d10a1c16e510_hermes-l-ambre-des-merveilles-768x614.jpg";
            String imgLancome = "05099940-0e5f-4145-ab2e-fba6fbe2078d_Lancome-Tresor-Midnight-Rose-3-768x768.jpg";
            String imgLeLabo = "82d4af41-5bd7-4016-a29d-5c4bdd115ca4_L-e-La-bo-Ano-ther-1-3-768x768.png";
            String imgJpg = "3a2e324c-dbc2-4fc7-be9c-ccb9cbf98ee5_jean-paul-gaultier-le-beau-paradise-garden-75ml.jpg";
            String imgBossPatchouli = "ff5571cc-1377-4a9f-8497-877e1321eac8_Nước Hoa Boss The Collection Cashmere Patchouli.jpg";
            String imgLvBeach = "0700b789-4065-4c2a-9427-23bd0f1ebb8c_O-n-T-he-Be-ach-10ml-768x768.png";
            String imgDiptyque = "52342992-bff8-44c7-a35b-c9d2149e55bf_Nuoc-hoa-Diptyque-Fleur-de-Peau-e1675071937586.png";
            String imgGucci = "91870e64-277f-4eb2-a656-e18a43b7d080_Gucci Premiere.jpg";

            // 1. Chanel Bleu de Chanel
            createPerfume("Chanel Bleu de Chanel Parfum", "Chanel", "Hương Gỗ Thơm",
                    "Bleu de Chanel Parfum mang hương thơm gỗ nồng nàn và đẳng cấp cho phái mạnh, đại diện cho tự do và bản lĩnh kiên định.",
                    "Lịch lãm, Sang trọng, Nam tính", "Lâu - 8 giờ đến 12 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Nam", imgBleu,
                    new String[][]{{"10ml", "450.000 đ", "50"}, {"50ml", "2.400.000 đ", "30"}, {"100ml", "3.900.000 đ", "20"}});

            // 2. Dior Poison Girl
            createPerfume("Dior Poison Girl EDP", "Dior", "Hương Phương Đông Ấm Áp",
                    "Poison Girl là hương thơm ngọt ngào, quyến rũ dành cho những cô nàng hiện đại, nổi loạn và tràn đầy năng lượng.",
                    "Ngọt ngào, Táo bạo, Gợi cảm", "Lâu - 7 giờ đến 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nữ", imgPoison,
                    new String[][]{{"10ml", "390.000 đ", "60"}, {"50ml", "2.150.000 đ", "35"}, {"100ml", "3.200.000 đ", "20"}});

            // 3. Hermes L'Ambre des Merveilles
            createPerfume("Hermes L'Ambre des Merveilles", "Hermes", "Hương Phương Đông Gỗ",
                    "Sự kết hợp ấm áp, bí ẩn giữa hổ phách và hoắc hương tạo nên một mùi hương unisex sang trọng, tao nhã.",
                    "Ấm áp, Sang trọng, Tinh tế", "Lâu - 7 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Unisex", imgHermes,
                    new String[][]{{"10ml", "350.000 đ", "50"}, {"50ml", "1.850.000 đ", "30"}, {"100ml", "2.650.000 đ", "25"}});

            // 4. Lancome Tresor Midnight Rose
            createPerfume("Lancome Tresor Midnight Rose", "Lancome", "Hương Hoa Cỏ Gỗ",
                    "Tresor Midnight Rose lãng mạn như câu chuyện tình Paris với quả mâm xôi chín đỏ ngọt ngào và đóa hoa hồng đen say đắm ẩn hiện.",
                    "Lãng mạn, Ngọt ngào, Gợi cảm", "Lâu - 8 giờ đến 10 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nữ", imgLancome,
                    new String[][]{{"10ml", "390.000 đ", "50"}, {"75ml", "2.450.000 đ", "30"}});

            // 5. Le Labo Another 13
            createPerfume("Le Labo Another 13 EDP", "Le Labo", "Hương Hoa Cỏ Gỗ Xạ Hương",
                    "Another 13 mang hương thơm độc đáo tựa như mùi hương da thịt tự nhiên sạch sẽ, thanh lịch nhưng vô cùng lôi cuốn.",
                    "Độc đáo, Hiện đại, Thanh lịch", "Rất lâu - Trên 12 giờ", "Gần - Trong vòng một cánh tay", "Mỹ", "Unisex", imgLeLabo,
                    new String[][]{{"10ml", "790.000 đ", "40"}, {"50ml", "3.800.000 đ", "20"}, {"100ml", "5.800.000 đ", "15"}});

            // 6. JPG Le Beau Paradise Garden
            createPerfume("JPG Le Beau Paradise Garden", "Jean Paul Gaultier", "Hương Hoa Cỏ Cam Chanh",
                    "Một kiệt tác tươi mát lấy cảm hứng từ khu vườn địa đàng với hương dừa ngào ngạt kết hợp bạc hà mát lạnh.",
                    "Tươi mát, Gợi cảm, Phóng khoáng", "Lâu - 8 giờ đến 12 giờ", "Xa - Toả hương trong vòng bán kính 2 mét", "Pháp", "Nam", imgJpg,
                    new String[][]{{"10ml", "380.000 đ", "50"}, {"75ml", "2.100.000 đ", "30"}, {"125ml", "2.850.000 đ", "20"}});

            // 7. Boss Cashmere Patchouli
            createPerfume("Hugo Boss Cashmere Patchouli", "Hugo Boss", "Hương Gỗ Cay Nồng",
                    "Mô phỏng chất liệu vải cashmere mịn màng, với hoắc hương nồng đượm, sô cô la ấm và gỗ tuyết tùng trầm ấm.",
                    "Ấm áp, Sang trọng, Độc đáo", "Lâu - 8 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Đức", "Nam", imgBossPatchouli,
                    new String[][]{{"10ml", "420.000 đ", "40"}, {"100ml", "3.600.000 đ", "20"}});

            // 8. Louis Vuitton On The Beach
            createPerfume("Louis Vuitton On The Beach EDP", "Louis Vuitton", "Hương Thơm Biển",
                    "Hương cam yuzu Nhật Bản mát rượi quyện hương cát nóng ấm và thảo mộc tự nhiên vùng biển Thái Bình Dương thơ mộng.",
                    "Mát lạnh, Sang trọng, Nhẹ nhàng", "Tạm ổn - 6 giờ đến 8 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Unisex", imgLvBeach,
                    new String[][]{{"10ml", "820.000 đ", "30"}, {"100ml", "7.200.000 đ", "15"}});

            // 9. Diptyque Fleur de Peau
            createPerfume("Diptyque Fleur de Peau EDP", "Diptyque", "Hương Hoa Cỏ Gỗ Xạ Hương",
                    "Fleur de Peau mang hương da thịt mềm mại, hương phấn thơm hoài cổ từ diên vĩ quý giá quyện lẫn xạ hương đầy gọi mời.",
                    "Thanh nhã, Tự nhiên, Quyến rũ", "Lâu - 8 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Pháp", "Unisex", imgDiptyque,
                    new String[][]{{"10ml", "490.000 đ", "30"}, {"75ml", "3.450.000 đ", "20"}});

            // 10. Gucci Premiere For Women
            createPerfume("Gucci Premiere For Women EDP", "Gucci", "Hương Hoa Cỏ Gỗ",
                    "Lấy cảm hứng từ những chiếc váy dạ hội lộng lẫy tại liên hoan phim Cannes, Gucci Premiere tôn vinh nét quyến rũ quý phái.",
                    "Kiêu sa, Tinh tế, Nữ tính", "Lâu - 7 giờ đến 10 giờ", "Gần - Trong vòng một cánh tay", "Ý", "Nữ", imgGucci,
                    new String[][]{{"10ml", "380.000 đ", "45"}, {"50ml", "1.950.000 đ", "25"}, {"100ml", "2.850.000 đ", "20"}});

            System.out.println("✅ Đã khởi tạo thành công 10 sản phẩm nước hoa mẫu tiêu biểu kèm các biến thể!");
        }
    }

    private void createPerfume(String ten, String hang, String nhomHuong, String moTa, String phongCach,
                               String luuHuong, String toaHuong, String xuatXu, String gioiTinh, String hinhAnh,
                               String[][] variantsData) {
        Perfume p = new Perfume();
        p.setTen_sp(ten);
        p.setThuong_hieu(hang);
        p.setNhom_huong(nhomHuong);
        p.setMo_ta(moTa);
        p.setPhong_cach(phongCach);
        p.setLuu_huong(luuHuong);
        p.setToa_huong(toaHuong);
        p.setXuat_xu(xuatXu);
        p.setGioi_tinh(gioiTinh);
        p.setHinh_anh("/uploads/" + hinhAnh);

        ArrayList<PerfumeVariant> list = new ArrayList<>();
        int totalStock = 0;
        for (String[] vData : variantsData) {
            PerfumeVariant pv = new PerfumeVariant();
            pv.setDung_tich(vData[0]);
            pv.setGia_ban(vData[1]);
            int stock = Integer.parseInt(vData[2]);
            pv.setSo_luong_ton(stock);
            pv.setPerfume(p);
            list.add(pv);
            totalStock += stock;
        }
        p.setVariants(list);
        p.setTon_kho(totalStock);

        perfumeRepository.save(p);
    }
}