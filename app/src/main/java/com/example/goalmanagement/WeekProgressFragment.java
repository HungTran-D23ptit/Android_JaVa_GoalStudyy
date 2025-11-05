package com.example.goalmanagement;

import android.os.Bundle;
import androidx.annotation.NonNull; // Thêm import
import androidx.annotation.Nullable; // Thêm import
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Import thêm các View bạn cần ánh xạ, ví dụ: import android.widget.TextView; import android.widget.ProgressBar;

/**
 * Fragment để hiển thị tiến độ học tập theo tuần.
 */
public class WeekProgressFragment extends Fragment {

    // TODO: Khai báo các biến View cần cập nhật dữ liệu
    // Ví dụ:
    // TextView tvTotalHoursThisWeek, tvTotalHoursLastWeek, tvAverageHours;
    // ProgressBar progressCompletedWeek, progressSkippedWeek, progressPostponedWeek;
    // Các View cho cột biểu đồ (nếu cần thay đổi chiều cao động)

    // Constructor rỗng là bắt buộc
    public WeekProgressFragment() {}

    @Nullable // Đổi thành Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout (nạp giao diện) cho Fragment này
        View view = inflater.inflate(R.layout.fragment_week_progress, container, false);

        // --- Ánh xạ View nên thực hiện ở đây hoặc trong onViewCreated ---
        // Ví dụ:
        // tvTotalHoursThisWeek = view.findViewById(R.id.tv_total_hours_this_week); // Giả sử bạn thêm ID này
        // progressCompletedWeek = view.findViewById(R.id.progress_completed_week);
        // ... ánh xạ các View khác

        return view; // Trả về View đã được tạo
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy số task hoàn thành/dời/chưa làm trong tuần hiện tại và tuần trước (đơn giản)
        new Thread(() -> {
            try {
                com.example.goalmanagement.data.AppDatabase db = com.example.goalmanagement.data.AppDatabase.getInstance(requireContext().getApplicationContext());
                java.text.SimpleDateFormat keyFmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Calendar c = java.util.Calendar.getInstance();
                // Tính range tuần hiện tại (Thứ 2 -> CN)
                c.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
                String weekStart = keyFmt.format(c.getTime());
                c.add(java.util.Calendar.DATE, 6);
                String weekEnd = keyFmt.format(c.getTime());
                // Tuần trước
                c.add(java.util.Calendar.DATE, -13);
                String prevStart = keyFmt.format(c.getTime());
                c.add(java.util.Calendar.DATE, 6);
                String prevEnd = keyFmt.format(c.getTime());

                int doneThis = db.taskDao().countByDayRangeAndStatus(weekStart, weekEnd, "completed");
                int postThis = db.taskDao().countByDayRangeAndStatus(weekStart, weekEnd, "postponed");
                int pendThis = db.taskDao().countByDayRangeAndStatus(weekStart, weekEnd, "pending");

                int donePrev = db.taskDao().countByDayRangeAndStatus(prevStart, prevEnd, "completed");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Tối thiểu: hiển thị bằng Toast (nếu layout chưa có view động)
                        android.widget.Toast.makeText(getContext(),
                                "Tuần này: Done=" + doneThis + ", Dời=" + postThis + ", Chờ=" + pendThis + "\nTuần trước: Done=" + donePrev,
                                android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception ignored) { }
        }).start();
    }

    // (Hàm ví dụ - bạn sẽ cần thay thế bằng logic thực tế)
    // private WeeklyStats loadWeeklyStatsData() {
    //     // ... Lấy dữ liệu từ nguồn ...
    //     return new WeeklyStats(...);
    // }

    // (Hàm ví dụ - bạn sẽ cần thay thế bằng logic thực tế)
    // private void updateBarChart(float[] dailyHours) {
    //     // ... Tìm các View cột và đặt lại layout_height dựa trên dailyHours ...
    // }

} // Kết thúc class WeekProgressFragment