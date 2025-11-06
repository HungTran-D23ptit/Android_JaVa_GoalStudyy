package com.example.goalmanagement;

import android.os.Bundle;
import androidx.annotation.NonNull; // Thêm import
import androidx.annotation.Nullable; // Thêm import
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Fragment để hiển thị tiến độ học tập theo tuần.
 */
public class WeekProgressFragment extends Fragment {

    // Khai báo các biến View cần cập nhật dữ liệu
    TextView tvTotalHoursThisWeek, tvTotalHoursLastWeek, tvAverageHours;
    ProgressBar progressCompletedWeek, progressSkippedWeek, progressPostponedWeek;
    // Các View cho cột biểu đồ (nếu cần thay đổi chiều cao động)

    // Constructor rỗng là bắt buộc
    public WeekProgressFragment() {}

    @Nullable // Đổi thành Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout (nạp giao diện) cho Fragment này
        View view = inflater.inflate(R.layout.fragment_week_progress, container, false);

        // Ánh xạ View
        progressCompletedWeek = view.findViewById(R.id.progress_completed_week);
        progressSkippedWeek = view.findViewById(R.id.progress_skipped_week);
        progressPostponedWeek = view.findViewById(R.id.progress_postponed_week);

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

                int totalThis = doneThis + postThis + pendThis;
                int skippedThis = pendThis; // Assuming skipped is pending, adjust as needed

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Update ProgressBars
                        if (progressCompletedWeek != null && totalThis > 0) {
                            progressCompletedWeek.setProgress((doneThis * 100) / totalThis);
                        }
                        if (progressPostponedWeek != null && totalThis > 0) {
                            progressPostponedWeek.setProgress((postThis * 100) / totalThis);
                        }
                        if (progressSkippedWeek != null && totalThis > 0) {
                            progressSkippedWeek.setProgress((skippedThis * 100) / totalThis);
                        }
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

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this tab
        onViewCreated(getView(), null);
    }

    // (Hàm ví dụ - bạn sẽ cần thay thế bằng logic thực tế)
    // private void updateBarChart(float[] dailyHours) {
    //     // ... Tìm các View cột và đặt lại layout_height dựa trên dailyHours ...
    // }

} // Kết thúc class WeekProgressFragment
